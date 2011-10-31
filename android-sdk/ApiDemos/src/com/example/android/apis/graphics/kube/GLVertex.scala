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

import java.nio.IntBuffer

class GLVertex(var x: Float, var y: Float, var z: Float, val index: Short) {
  import GLVertex._  // companion object

  // index in vertex table
  var color: GLColor = _

  def this() { this(0, 0, 0, -1) }

  override def equals(other: Any): Boolean =
    other match {
      case v: GLVertex => x == v.x && y == v.y && z == v.z
      case _ => false
    }

  def put(vertexBuffer: IntBuffer, colorBuffer: IntBuffer) {
    vertexBuffer put toFixed(x)
    vertexBuffer put toFixed(y)
    vertexBuffer put toFixed(z)
    if (color == null) {
      colorBuffer put 0
      colorBuffer put 0
      colorBuffer put 0
      colorBuffer put 0
    } else {
      colorBuffer put color.red
      colorBuffer put color.green
      colorBuffer put color.blue
      colorBuffer put color.alpha
    }
  }

  def update(vertexBuffer: IntBuffer, transform: M4) {
    // skip to location of vertex in mVertex buffer
    vertexBuffer.position(index * 3)
    if (transform == null) {
      vertexBuffer put toFixed(x)
      vertexBuffer put toFixed(y)
      vertexBuffer put toFixed(z)
    } else {
      val temp = new GLVertex()
      transform.multiply(this, temp)
      vertexBuffer put toFixed(temp.x)
      vertexBuffer put toFixed(temp.y)
      vertexBuffer put toFixed(temp.z)
    }
  }
}

object GLVertex {
  private def toFixed(x: Float): Int = (x * 65536.0f).toInt
}

