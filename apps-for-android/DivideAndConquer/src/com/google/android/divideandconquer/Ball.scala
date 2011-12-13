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

/**
 * A ball has a current location, a trajectory angle, a speed in pixels per
 * second, and a last update time.  It is capable of updating itself based on
 * its trajectory and speed.
 *
 * It also knows its boundaries, and will 'bounce' off them when it reaches them.
 */
class Ball(now: Long, pixelsPerSecond: Float, x: Float, y: Float,
           angle: Double, radiusPixels: Float) extends Shape2d {
  import Ball._  // companion object

  private var mLastUpdate = now
  private var mX = x
  private var mY = y
  private var mAngle = angle

  private val mPixelsPerSecond = pixelsPerSecond
  private val mRadiusPixels = radiusPixels

  private var mRegion: Shape2d = _

  def getX: Float = mX

  def getY: Float = mY

  def getLeft: Float = mX - mRadiusPixels

  def getRight: Float = mX + mRadiusPixels

  def getTop: Float = mY - mRadiusPixels

  def getBottom: Float = mY + mRadiusPixels

  def getRadiusPixels: Float = mRadiusPixels

  def getAngle: Double = mAngle

  /**
   * Get the region the ball is contained in.
   */
  def getRegion: Shape2d = mRegion

  /**
   * Set the region that the ball is contained in.
   * @param region The region.
   */
  def setRegion(region: Shape2d) {
    if (mX < region.getLeft) {
      mX = region.getLeft
      bounceOffLeft()
    } else if (mX > region.getRight) {
      mX = region.getRight
      bounceOffRight()
    }
    if (mY < region.getTop) {
      mY = region.getTop
      bounceOffTop()
    } else if (mY > region.getBottom) {
      mY = region.getBottom
      bounceOffBottom()
    }
    mRegion = region
  }

  def setNow(now: Long) { mLastUpdate = now }

  def isCircleOverlapping(otherBall: Ball): Boolean = {
    val dy = otherBall.mY - mY
    val dx = otherBall.mX - mX

    val distance = dy * dy + dx * dx

    (distance < ((2 * mRadiusPixels) * (2 *mRadiusPixels))) &&
    // avoid jittery collisions
    !movingAwayFromEachother(this, otherBall)
  }

  private def movingAwayFromEachother(ballA: Ball, ballB: Ball): Boolean = {
    val collA = math.atan2(ballB.mY - ballA.mY, ballB.mX - ballA.mX)
    val collB = math.atan2(ballA.mY - ballB.mY, ballA.mX - ballB.mX)

    val ax = math.cos(ballA.mAngle - collA)
    val bx = math.cos(ballB.mAngle - collB)

    ax + bx < 0
  }

  def update(now: Long) {
    if (now <= mLastUpdate) return

    // bounce when at walls
    if (mX <= mRegion.getLeft + mRadiusPixels) {
      // we're at left wall
      mX = mRegion.getLeft + mRadiusPixels
      bounceOffLeft()
    } else if (mY <= mRegion.getTop + mRadiusPixels) {
      // at top wall
      mY = mRegion.getTop + mRadiusPixels
      bounceOffTop();
    } else if (mX >= mRegion.getRight - mRadiusPixels) {
      // at right wall
      mX = mRegion.getRight - mRadiusPixels
      bounceOffRight()
    } else if (mY >= mRegion.getBottom - mRadiusPixels) {
      // at bottom wall
      mY = mRegion.getBottom - mRadiusPixels
      bounceOffBottom()
    }

    var delta = (now - mLastUpdate) * mPixelsPerSecond
    delta = delta / 1000f

    mX += (delta * math.cos(mAngle)).toFloat
    mY += (delta * math.sin(mAngle)).toFloat

    mLastUpdate = now
  }

  private def bounceOffBottom() {
    if (mAngle < PI_HALF) {
      // going right
      mAngle = -mAngle
    } else {
      // going left
      mAngle += (math.Pi - mAngle) * 2
    }
  }

  private def bounceOffRight() {
    if (mAngle > PI_ONEANDHALF) {
      // going up
      mAngle -= (mAngle - PI_ONEANDHALF) * 2
    } else {
      // going down
      mAngle += (PI_HALF - mAngle) * 2
    }
  }

  private def bounceOffTop() {
    if (mAngle < PI_ONEANDHALF) {
      // going left
      mAngle -= (mAngle - math.Pi) * 2;
    } else {
      // going right
      mAngle += (PI_TWICE - mAngle) * 2
      mAngle -= PI_TWICE
    }
  }

  private def bounceOffLeft() {
    if (mAngle < math.Pi) {
      // going down
      mAngle -= ((mAngle - PI_HALF) * 2)
    } else {
      // going up
      mAngle += ((PI_ONEANDHALF - mAngle) * 2)
    }
  }

  override def toString: String =
    "Ball(x=%f, y=%f, angle=%f)".format(mX, mY, math.toDegrees(mAngle))

}

object Ball {

  final val PI_HALF       = 0.5 * math.Pi
  final val PI_ONEANDHALF = 1.5 * math.Pi
  final val PI_TWICE      = 2.0 * math.Pi

  /**
   * Given that ball a and b have collided, adjust their angles to reflect
   * their state after the collision.
   *
   * This method works based on the conservation of energy and momentum in
   * an elastic collision.  Because the balls have equal mass and speed, it
   * ends up being that they simply swap velocities along the axis of the
   * collision, keeping the velocities tangent to the collision constant.
   *
   * @param ballA The first ball in a collision
   * @param ballB The second ball in a collision
   */
  def adjustForCollision(ballA: Ball, ballB: Ball) {

    val collA = math.atan2(ballB.mY - ballA.mY, ballB.mX - ballA.mX)
    val collB = math.atan2(ballA.mY - ballB.mY, ballA.mX - ballB.mX)

    val ax = math.cos(ballA.mAngle - collA)
    val ay = math.sin(ballA.mAngle - collA)

    val bx = math.cos(ballB.mAngle - collB)
    val by = math.cos(ballB.mAngle - collB)

    val diffA = math.atan2(ay, -bx)
    val diffB = math.atan2(by, -ax)

    ballA.mAngle = collA + diffA
    ballB.mAngle = collB + diffB
  }

  /**
   * A more readable way to create balls than using a 5 param
   * constructor of all numbers.
   */
  class Builder {
    private var mNow: Long = -1
    private var mX: Float = -1
    private var mY: Float = -1
    private var mAngle: Double = -1
    private var mRadiusPixels: Float = -1

    private var mPixelsPerSecond = 45f

    def create(): Ball = {
      if (mNow < 0)
        throw new IllegalStateException("must set 'now'")

      if (mX < 0)
        throw new IllegalStateException("X must be set")

      if (mY < 0)
        throw new IllegalStateException("Y must be stet")

      if (mAngle < 0)
        throw new IllegalStateException("angle must be set")

      if (mAngle > 2 * math.Pi)
        throw new IllegalStateException("angle must be less that 2Pi")

      if (mRadiusPixels <= 0)
        throw new IllegalStateException("radius must be set")

      new Ball(mNow, mPixelsPerSecond, mX, mY, mAngle, mRadiusPixels)
    }

    def setNow(now: Long): Builder = {
      mNow = now
      this
    }

    def setPixelsPerSecond(pixelsPerSecond: Float): Builder = {
      mPixelsPerSecond = pixelsPerSecond
      this
    }

    def setX(x: Float): Builder = {
      mX = x
      this
    }

    def setY(y: Float): Builder = {
      mY = y
      this
    }

    def setAngle(angle: Double): Builder = {
      mAngle = angle
      this
    }

    def setRadiusPixels(pixels: Float): Builder = {
      mRadiusPixels = pixels
      this
    }
  }
}
