/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics.kube

class Layer(axis: Int) {
  import Layer._  // companion object

  private[kube] val mShapes = new Array[GLShape](9)
  private val mTransform = new M4()

  // start with identity matrix for transformation
  mTransform.setIdentity()
  
  def startAnimation() {
    for (shape <- mShapes if shape != null)
      shape.startAnimation()
  }

  def endAnimation() {
    for (shape <- mShapes if shape != null)
      shape.endAnimation()
  }

  def setAngle(angle: Float) {
    import M4._
    // normalize the angle
    val twopi = (math.Pi *2f).toFloat
    var _angle = angle
    while (_angle >= twopi) _angle -= twopi
    while (_angle < 0f) _angle += twopi
    
    val sin = math.sin(_angle).toFloat
    val cos = math.cos(_angle).toFloat

    val m = mTransform.m
    axis match {
      case AXIS_X =>
        m(N+5) = cos
        m(N+2) = sin
        m(2*N+1) = -sin
        m(2*N+2) = cos
        m(0) = 1f
        m(1) = 0f
        m(2) = 0f
        m(N) = 0f
        m(2*N) = 0f

      case AXIS_Y =>
        m(0) = cos
        m(2) = sin
        m(2*N) = -sin
        m(2*N+2) = cos
        m(N+1) = 1f
        m(1) = 0f
        m(N) = 0f
        m(N+2) = 0f
        m(2*N+1) = 0f

      case AXIS_Z =>
        m(0) = cos
        m(1) = sin
        m(N) = -sin
        m(N+1) = cos
        m(2*N+2) = 1f
        m(2*N) = 0f
        m(2*N+1) = 0f
        m(2) = 0f
        m(N+2) = 0f
    }

    for (shape <- mShapes if shape != null)
      shape animateTransform mTransform
  }
}

object Layer {
  // which axis do we rotate around?
  // 0 for X, 1 for Y, 2 for Z
  final val AXIS_X = 0
  final val AXIS_Y = 1
  final val AXIS_Z = 2
}
