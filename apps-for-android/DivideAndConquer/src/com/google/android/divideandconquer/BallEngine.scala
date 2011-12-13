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

import android.content.Context
import android.util.Log
import android.widget.Toast

import scala.collection.mutable.ListBuffer

/**
 * Keeps track of the current state of balls bouncing around within a a set of
 * regions.
 *
 * Note: 'now' is the elapsed time in milliseconds since some consistent point in time.
 * As long as the reference point stays consistent, the engine will be happy, though
 * typically this is {@link android.os.SystemClock#elapsedRealtime()} 
 */
class BallEngine(minX: Float, maxX: Float,
                 minY: Float, maxY: Float,
                 ballSpeed: Float, ballRadius: Float) {
  import BallEngine._  // companion object

  private var mCallBack: BallEventCallBack = _

  /**
   * Holds onto new regions during a split
   */
  private val mNewRegions = new ListBuffer[BallRegion]//(8)
  private val mOldRegions = new ListBuffer[BallRegion]
  private val mRegions    = new ListBuffer[BallRegion]//(8)

  def setCallBack(callBack: BallEventCallBack) {
    mCallBack = mCallBack
  }

  /**
   * Update the notion of 'now' in milliseconds.  This can be usefull
   * when unpausing for instance.
   * @param now Milliseconds since some consistent point in time.
   */
  def setNow(now: Long) {
    for (region <- mRegions) region setNow now
  }

  /**
   * Rest the engine back to a single region with a certain number of balls
   * that will be placed randomly and sent in random directions.
   * @param now milliseconds since some consistent point in time.
   * @param numBalls
   */
  def reset(now: Long, numBalls: Int) {
    mRegions.clear()

    val balls = List.fill(numBalls)(
      new Ball.Builder()
        .setNow(now)
        .setPixelsPerSecond(ballSpeed)
        .setAngle(math.random * Ball.PI_TWICE)
        .setX(math.random.toFloat * (maxX - minX) + minX)
        .setY(math.random.toFloat * (maxY - minY) + minY)
        .setRadiusPixels(ballRadius)
        .create()
    )
    val region = new BallRegion(now, minX, maxX, minY, maxY, balls)
    region setCallBack mCallBack

    mRegions += region
  }

  def getRegions: List[BallRegion] = mRegions.toList

  def getPercentageFilled: Float = {
    val total = mRegions map (_.getArea) reduceLeft (_ + _)
    1f - (total / getArea)
  }

  /**
   * @return the area in the region in pixel*pixel
   */
  def getArea: Float = (maxX - minX) * (maxY - minY)

  /**
   * Can any of the regions within start a line at this point?
   * @param x The x coordinate.
   * @param y The y coordinate
   * @return Whether a region can start a line.
   */
  def canStartLineAt(x: Float, y: Float): Boolean =
    mRegions exists (_.canStartLineAt(x, y))

  /**
   * Start a horizontal line at a certain point.
   * @throws IllegalArgumentException if there is no region that can start a
   *     line at the point.
   */
  def startHorizontalLine(now: Long, x: Float, y: Float) {
    for (region <- mRegions) {
      if (region.canStartLineAt(x, y)) {
        region.startHorizontalLine(now, x, y)
        return
      }
    }
    throw new IllegalArgumentException("no region can start a new line at "
                + x + ", " + y + ".")
  }

  /**
   * Start a vertical line at a certain point.
   * @throws IllegalArgumentException if there is no region that can start a
   *     line at the point.
   */
  def startVerticalLine(now: Long, x: Float, y: Float) {
    for (region <- mRegions) {
      if (region.canStartLineAt(x, y)) {
        region.startVerticalLine(now, x, y)
        return
      }
    }
    throw new IllegalArgumentException("no region can start a new line at "
                + x + ", " + y + ".")
  }

  /**
   * @param now The latest notion of 'now'
   * @return whether any new regions were added by the update.
   */
  def update(now: Long): Boolean = {
    var regionChange = false
    for (region <- mRegions) {
      val newRegion = region update now

      if (newRegion != null) {
        regionChange = true
        if (!newRegion.getBalls.isEmpty) {
          mNewRegions += newRegion
        }

        // current region may not have any balls left
        if (region.getBalls.isEmpty) {
          mOldRegions -= region
        }
      } else if (region.consumeDoneShrinking()) {
        regionChange = true
      }
    }
    mRegions --= mOldRegions
    mRegions ++= mNewRegions
    mNewRegions.clear()
    mOldRegions.clear()

    regionChange
  }
}

object BallEngine {
  trait BallEventCallBack {
    def onBallHitsBall(ballA: Ball, ballB: Ball)
    def onBallHitsLine(when: Long, ball: Ball, animatingLine: AnimatingLine)
  }
}
