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

import android.util.Log

import java.nio.ShortBuffer
import java.util.ArrayList

class GLFace() {
  
  // for triangles
  def this(v1: GLVertex, v2: GLVertex, v3: GLVertex) {
    this()
    addVertex(v1)
    addVertex(v2)
    addVertex(v3)
  }  
  // for quadrilaterals
  def this(v1: GLVertex, v2: GLVertex, v3: GLVertex, v4: GLVertex) {
    this(v1, v2, v3)
    addVertex(v4)
  }

  def addVertex(v: GLVertex) {
    mVertexList add v
  }

  // must be called after all vertices are added
  def setColor(c: GLColor) {
    
    val last = mVertexList.size - 1
    if (last < 2) {
      Log.e("GLFace", "not enough vertices in setColor()");
    } else {
      var vertex = mVertexList get last
      
      // only need to do this if the color has never been set
      if (mColor == null) {
        while (vertex.color != null) {
          mVertexList.add(0, vertex)
          mVertexList.remove(last + 1)
          vertex = mVertexList get last
        }
      }
      
      vertex.color = c
    }

    mColor = c
  }
  
  def getIndexCount: Int = (mVertexList.size - 2) * 3
  
  def putIndices(buffer: ShortBuffer) {
    val last = mVertexList.size - 1

    var v0 = mVertexList get 0
    val vn = mVertexList get last
    
    // push triangles into the buffer
    for (i <- 1 until last) {
      val v1 = mVertexList get i
      buffer put v0.index
      buffer put v1.index
      buffer put vn.index
      v0 = v1
    }
  }
  
  private val mVertexList = new ArrayList[GLVertex]()
  private var mColor: GLColor = _
}
