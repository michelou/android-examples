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
 * A 2d shape has left, right, top and bottom dimensions.
 *
 */
abstract class Shape2d {

  def getLeft: Float
  def getRight: Float
  def getTop: Float
  def getBottom: Float

  /**
   * @param other Another 2d shape
   * @return Whether this shape is intersecting with the other.
   */
  def isIntersecting(other: Shape2d): Boolean =
    getLeft <= other.getRight && getRight >= other.getLeft &&
    getTop <= other.getBottom && getBottom >= other.getTop

  /**
   * @param x An x coordinate
   * @param y A y coordinate
   * @return Whether the point is within this shape
   */
  def isPointWithin(x: Float, y: Float): Boolean =
    x > getLeft && x < getRight && y > getTop && y < getBottom

  def getArea: Float = getHeight * getWidth

  def getHeight: Float = getBottom - getTop

  def getWidth: Float = getRight - getLeft
}
