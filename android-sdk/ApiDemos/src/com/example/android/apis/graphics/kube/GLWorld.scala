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

import java.nio.{ByteBuffer, ByteOrder, IntBuffer, ShortBuffer}
import java.util.{Iterator, ArrayList}

import javax.microedition.khronos.opengles.GL10

class GLWorld {

  def addShape(shape: GLShape) {
    mShapeList add shape
    mIndexCount += shape.getIndexCount
  }
  
  def generate() {    
    var bb = ByteBuffer.allocateDirect(mVertexList.size*4*4)
    bb order ByteOrder.nativeOrder()
    mColorBuffer = bb.asIntBuffer()

    bb = ByteBuffer.allocateDirect(mVertexList.size*4*3)
    bb order ByteOrder.nativeOrder()
    mVertexBuffer = bb.asIntBuffer()

    bb = ByteBuffer.allocateDirect(mIndexCount*2)
    bb order ByteOrder.nativeOrder()
    mIndexBuffer = bb.asShortBuffer()

    val iter2 = mVertexList.iterator()
    while (iter2.hasNext) {
      val vertex = iter2.next()
      vertex.put(mVertexBuffer, mColorBuffer)
    }

    val iter3 = mShapeList.iterator()
    while (iter3.hasNext) {
      val shape = iter3.next()
      shape.putIndices(mIndexBuffer)
    }
  }
  
  def addVertex(x: Float, y: Float, z: Float): GLVertex = {
    val vertex = new GLVertex(x, y, z, mVertexList.size.toShort)
    mVertexList add vertex
    vertex
  }
  
  def transformVertex(vertex: GLVertex, transform: M4) {
    vertex.update(mVertexBuffer, transform)
  }

  var count = 0

  def draw(gl: GL10) {
    mColorBuffer position 0
    mVertexBuffer position 0
    mIndexBuffer position 0

    gl glFrontFace GL10.GL_CW
    gl glShadeModel GL10.GL_FLAT
    gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer)
    gl.glColorPointer(4, GL10.GL_FIXED, 0, mColorBuffer)
    gl.glDrawElements(GL10.GL_TRIANGLES, mIndexCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer)
    count += 1
  }

  private val mShapeList = new ArrayList[GLShape]()
  private val mVertexList = new ArrayList[GLVertex]()
  
  private var mIndexCount: Int = 0

  private var mVertexBuffer: IntBuffer = _
  private var mColorBuffer: IntBuffer = _
  private var mIndexBuffer: ShortBuffer = _
}
