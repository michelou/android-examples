/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.divideandconquer

import java.lang.ref.WeakReference

import scala.collection.mutable.ListBuffer
import scala.util.control.Breaks._

/**
 * A ball region is a rectangular region that contains bouncing balls,
 * and possibly one animating line.  In its {@link #update(long)} method,
 * it will update all of its balls, the moving line.  It detects collisions
 * between the balls and the moving line, and when the line is complete,
 * handles splitting off a new region.
 */
class BallRegion(now: Long, left: Float, right: Float,
                 top: Float, bottom: Float,
                 balls: List[Ball]) extends Shape2d {
  import BallRegion._  // companion object

  private var mLeft: Float = left
  private var mRight: Float = right
  private var mTop: Float = top
  private var mBottom: Float = bottom

  private val mBalls = ListBuffer(balls: _*)
  for (ball <- mBalls) ball setRegion this
  checkShrinkToFit()

  private var mAnimatingLine: AnimatingLine = _

  private var mShrinkingToFit = false
  private var mLastUpdate = now

  private var mDoneShrinking = false

  private var mCallBack: WeakReference[BallEngine.BallEventCallBack] = _

  def setCallBack(callBack: BallEngine.BallEventCallBack) {
    mCallBack = new WeakReference[BallEngine.BallEventCallBack](callBack)
  }

  private def checkShrinkToFit() {
    val area = getArea
    if (area < SHRINK_TO_FIT_AREA_THRESH)
      mShrinkingToFit = true
    else if (area < SHRINK_TO_FIT_AREA_THRESH_ONE_BALL && mBalls.size == 1)
      mShrinkingToFit = true
  }

  def getLeft: Float = mLeft

  def getRight: Float = mRight

  def getTop: Float = mTop

  def getBottom: Float = mBottom

  def getBalls: List[Ball] = mBalls.toList

  def getAnimatingLine: AnimatingLine = mAnimatingLine

  def consumeDoneShrinking(): Boolean = {
    if (mDoneShrinking) {
      mDoneShrinking = false
      true
    } else
      false
  }

  def setNow(now: Long) {
    mLastUpdate = now

    // update the balls
    for (ball <- mBalls) ball setNow now

    if (mAnimatingLine != null)
      mAnimatingLine setNow now
  }

  /**
   * Update the balls an (if it exists) the animating line in this region.
   * @param now in millis
   * @return A new region if a split has occured because the animating line
   *     finished.
   */
  def update(now: Long): BallRegion = {
    // update the animating line
    val newRegion = mAnimatingLine != null && mAnimatingLine.update(now)

    // move balls, check for collision with animating line
    for (ball <- mBalls) {
      ball update now
      if (mAnimatingLine != null && ball.isIntersecting(mAnimatingLine)) {
        mAnimatingLine = null
        if (mCallBack != null) mCallBack.get.onBallHitsLine(now, ball, mAnimatingLine)
      }
    }

    val numBalls = mBalls.size
    var found = false

    // update ball to ball collisions
    breakable {
      for (i <- 0 until numBalls; ball = mBalls(i);
           j <- i + 1 until numBalls; other = mBalls(j)) {
        if (ball isCircleOverlapping other) {
          Ball.adjustForCollision(ball, other)
          break()
        }
      }
    }        

    handleShrinkToFit(now)

    // no collsion, new region means we need to split out the apropriate
    // balls into a new region
    if (newRegion && mAnimatingLine != null) {
      val otherRegion = splitRegion(
                    now,
                    mAnimatingLine.getDirection,
                    mAnimatingLine.getPerpAxisOffset)
      mAnimatingLine = null
      otherRegion
    } else
      null
  }

  private def handleShrinkToFit(now: Long) {
    // update shrinking to fit
    if (mShrinkingToFit && mAnimatingLine == null) {
      if (now == mLastUpdate) return
      var delta = (now - mLastUpdate) * PIXELS_PER_SECOND
      delta = delta / 1000

      if (getHeight > MIN_EDGE) {
        mTop += delta
        mBottom -= delta
      }
      if (getWidth > MIN_EDGE) {
        mLeft += delta
        mRight -= delta
      }

      for (ball <- mBalls) ball setRegion this

      if (getArea <= SHRINK_TO_FIT_AREA) {
        mShrinkingToFit = false
        mDoneShrinking = true
      }
    }
    mLastUpdate = now
  }

  /**
   * Return whether this region can start a line at a certain point.
   */
  def canStartLineAt(x: Float, y: Float): Boolean =
    !mShrinkingToFit && mAnimatingLine == null && isPointWithin(x, y)

  /**
   * Start a horizontal line at a point.
   * @param now What 'now' is.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  def startHorizontalLine(now: Long, x: Float, y: Float) {
    if (!canStartLineAt(x, y)) {
      throw new IllegalArgumentException(
                    "can't start line with point (" + x + "," + y + ")")
    }
    mAnimatingLine =
      new AnimatingLine(Direction.Horizontal, now, y, x, mLeft, mRight)
  }

  /**
   * Start a vertical line at a point.
   * @param now What 'now' is.
   * @param x The x coordinate.
   * @param y The y coordinate.
   */
  def startVerticalLine(now: Long, x: Float, y: Float) {
    if (!canStartLineAt(x, y)) {
      throw new IllegalArgumentException(
                    "can't start line with point (" + x + "," + y + ")")
    }
    mAnimatingLine =
      new AnimatingLine(Direction.Vertical, now, x, y, mTop, mBottom)
  }

  /**
   * Splits this region at a certain offset, shrinking this one down and returning
   * the other region that makes up the rest.
   * @param direction The direction of the line.
   * @param perpAxisOffset The offset of the perpendicular axis of the line.
   * @return A new region containing a portion of the balls.
   */
  private def splitRegion(now: Long, direction: Direction, perpAxisOffset: Float): BallRegion = {
    val splitBalls = new ListBuffer[Ball]()

    if (direction == Direction.Horizontal) {
      for (ball <- mBalls if ball.getY > perpAxisOffset) {
        mBalls -= ball
        splitBalls += ball
      }
      val oldBottom = mBottom
      mBottom = perpAxisOffset
      checkShrinkToFit()
      val region = new BallRegion(now, mLeft, mRight, perpAxisOffset,
                    oldBottom, splitBalls.toList)
      region setCallBack mCallBack.get
      region
    } else {
      assert(direction == Direction.Vertical)
      for (ball <- mBalls if ball.getX > perpAxisOffset) {
        mBalls -= ball
        splitBalls += ball
      }
      val oldRight = mRight
      mRight = perpAxisOffset
      checkShrinkToFit()
      val region = new BallRegion(now, perpAxisOffset, oldRight, mTop,
                    mBottom, splitBalls.toList)
      region setCallBack mCallBack.get
      region
    }
  }

}

object BallRegion {
  private final val PIXELS_PER_SECOND = 25.0f

  private final val SHRINK_TO_FIT_AREA_THRESH = 10000.0f
  private final val SHRINK_TO_FIT_AREA_THRESH_ONE_BALL = 20000.0f
  private final val SHRINK_TO_FIT_AREA = 1000f
  private final val MIN_EDGE = 30f
}
