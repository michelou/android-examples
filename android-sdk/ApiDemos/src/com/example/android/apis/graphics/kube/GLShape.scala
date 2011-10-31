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

import java.nio.ShortBuffer

import scala.collection.mutable.ListBuffer

class GLShape(world: GLWorld) {
  
  def addFace(face: GLFace) { mFaceList += face }
  
  def setFaceColor(face: Int, color: GLColor) {
    mFaceList(face) setColor color }

  def putIndices(buffer: ShortBuffer) {
    for (face <- mFaceList) face putIndices buffer
  }

  def getIndexCount: Int = mFaceList.foldLeft(0){ _ + _.getIndexCount }

  def addVertex(x: Float, y: Float, z: Float): GLVertex =
    // look for an existing GLVertex first
    mVertexList find { v => v.x == x && v.y == y && v.z == z } match {
      case Some(vertex) =>
        vertex
      case None =>
        // doesn't exist, so create new vertex
        val vertex = world.addVertex(x, y, z)
        mVertexList += vertex
        vertex
    }

  def animateTransform(transform: M4) {
    mAnimateTransform = transform

    val newTransform =
      if (mTransform != null) mTransform multiply transform
      else transform

    for (vertex <- mVertexList)
      world.transformVertex(vertex, newTransform)
  }
  
  def startAnimation() { }

  def endAnimation() {
    mTransform =
      if (mTransform == null) new M4(mAnimateTransform)
      else mTransform multiply mAnimateTransform
  }

  private var mTransform: M4 = _
  private var mAnimateTransform: M4 = _
  protected val mFaceList = new ListBuffer[GLFace]()
  protected val mVertexList = new ListBuffer[GLVertex]()
  protected val mIndexList = new ListBuffer[Int]()
}
