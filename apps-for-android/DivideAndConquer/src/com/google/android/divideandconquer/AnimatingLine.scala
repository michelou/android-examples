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
 * Keeps the state for the line that extends in two directions until it hits
 * its boundaries.  This is triggered by the user gesture in a horizontal or
 * vertical direction.
 *
 * @param direction The direction of the line
 * @param now What 'now' is
 * @param axisStart Where on the perpindicular axis the line is extending from
 * @param start The point the line is extending from on the parallel axis
 * @param min The lower bound for the line (i.e the left most point)
 * @param max The upper bound for the line (i.e the right most point)
 */
class AnimatingLine(direction: Direction, now: Long,
                    axisStart: Float, start: Float,
                    min: Float, max: Float) extends Shape2d {

  private var mDirection: Direction = direction

  // for vertical lines, the y offset
  // for horizontal, the x offset
  private val mPerpAxisOffset = axisStart

  private var mStart = start
  private var mEnd = start

  private var mMin = min
  private var mMax = max

  private var mLastUpdate = 0l
  private var mPixelsPerSecond = 101.0f

  def getDirection: Direction = mDirection

  def getPerpAxisOffset: Float = mPerpAxisOffset

  def getStart: Float = mStart

  def getEnd: Float = mEnd

  def getMin: Float = mMin

  def getMax: Float = mMax

  def getLeft: Float =
    if (mDirection == Direction.Horizontal) mStart else mPerpAxisOffset

  def getRight: Float =
    if (mDirection == Direction.Horizontal) mEnd else mPerpAxisOffset

  def getTop: Float =
    if (mDirection == Direction.Vertical) mStart else mPerpAxisOffset

  def getBottom: Float =
    if (mDirection == Direction.Vertical) mEnd else mPerpAxisOffset

  def getPercentageDone: Float = (mEnd - mStart) / (mMax - mMin)

  /**
   * Extend the line according to the animation.
   * @return whether the line has reached its end.
   */
  def update(time: Long): Boolean = {
    if (time == mLastUpdate) return false
    var delta = (time - mLastUpdate) * mPixelsPerSecond
    delta = delta / 1000
    mLastUpdate = time
    mStart -= delta
    mEnd += delta

    if (mStart < mMin) mStart = mMin
    if (mEnd > mMax) mEnd = mMax

    mStart == mMin && mEnd == mMax
  }

  def setNow(now: Long) { mLastUpdate = now }
}
