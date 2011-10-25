/*
 * Copyright (C) 2007 Google Inc.
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

package com.google.android.opengles.spritetext

import java.nio.{CharBuffer, FloatBuffer}

import javax.microedition.khronos.opengles.GL10

/**
 * A 2D rectangular mesh. Can be drawn textured or untextured.
 *
 */
class Grid(w: Int, h: Int) {

  if (w < 0 || w >= 65536)
    throw new IllegalArgumentException("w")

  if (h < 0 || h >= 65536)
    throw new IllegalArgumentException("h")

  if (w * h >= 65536)
    throw new IllegalArgumentException("w * h >= 65536")

  mW = w
  mH = h

  {
    val size = w * h
    mVertexArray = new Array[Float](size * 3)
    mVertexBuffer = FloatBuffer.wrap(mVertexArray)

    mTexCoordArray = new Array[Float](size * 2)
    mTexCoordBuffer = FloatBuffer.wrap(mTexCoordArray)

    val quadW = mW - 1
    val quadH = mH - 1
    val quadCount = quadW * quadH
    val indexCount = quadCount * 6
    mIndexCount = indexCount
    val indexArray = new Array[Char](indexCount)

    /*
     * Initialize triangle list mesh.
     *
     *     [0]-----[  1] ...
     *      |    /   |
     *      |   /    |
     *      |  /     |
     *     [w]-----[w+1] ...
     *      |       |
     *
     */

    var i = 0
    for (y <- 0 until quadH; x <- 0 until quadW) {
      val a = (y * mW + x).toChar
      val b = (y * mW + x + 1).toChar
      val c = ((y + 1) * mW + x).toChar
      val d = ((y + 1) * mW + x + 1).toChar

      indexArray(i) = a; i += 1
      indexArray(i) = b; i += 1
      indexArray(i) = c; i += 1

      indexArray(i) = b; i += 1
      indexArray(i) = c; i += 1
      indexArray(i) = d; i += 1
    }
    mIndexBuffer = CharBuffer.wrap(indexArray)
  }

  def set(i: Int, j: Int, x: Float, y: Float, z: Float, u: Float, v: Float) {
    if (i < 0 || i >= mW) 
      throw new IllegalArgumentException("i")

    if (j < 0 || j >= mH)
      throw new IllegalArgumentException("j")

    val index = mW * j + i

    val posIndex = index * 3
    mVertexArray(posIndex) = x
    mVertexArray(posIndex + 1) = y
    mVertexArray(posIndex + 2) = z

    val texIndex = index * 2
    mTexCoordArray(texIndex) = u
    mTexCoordArray(texIndex + 1) = v
  }

  def draw(gl: GL10, useTexture: Boolean) {
    gl glEnableClientState GL10.GL_VERTEX_ARRAY
    gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer)

    if (useTexture) {
      gl glEnableClientState GL10.GL_TEXTURE_COORD_ARRAY
      gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexCoordBuffer)
      gl glEnable GL10.GL_TEXTURE_2D
    } else {
      gl glDisableClientState GL10.GL_TEXTURE_COORD_ARRAY
      gl glDisable GL10.GL_TEXTURE_2D
    }

    gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount,
        GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
    gl glDisableClientState GL10.GL_VERTEX_ARRAY
  }

  private var mVertexBuffer: FloatBuffer = _
  private var mVertexArray: Array[Float] = _

  private var mTexCoordBuffer: FloatBuffer = _
  private var mTexCoordArray: Array[Float] = _

  private var mIndexBuffer: CharBuffer = _

  private var mW: Int = _
  private var mH: Int = _
  private var mIndexCount: Int = _
}
