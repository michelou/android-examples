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


class Cube(world: GLWorld, left: Float, bottom: Float, back: Float,
           right: Float, top: Float, front: Float) extends GLShape(world) {

  private val leftBottomBack = addVertex(left, bottom, back)
  private val rightBottomBack = addVertex(right, bottom, back)
  private val leftTopBack = addVertex(left, top, back)
  private val rightTopBack = addVertex(right, top, back)
  private val leftBottomFront = addVertex(left, bottom, front)
  private val rightBottomFront = addVertex(right, bottom, front)
  private val leftTopFront = addVertex(left, top, front)
  private val rightTopFront = addVertex(right, top, front)

  // vertices are added in a clockwise orientation (when viewed from the outside)
  // bottom
  addFace(new GLFace(leftBottomBack, leftBottomFront, rightBottomFront, rightBottomBack))
  // front
  addFace(new GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront))
  // left
  addFace(new GLFace(leftBottomBack, leftTopBack, leftTopFront, leftBottomFront))
  // right
  addFace(new GLFace(rightBottomBack, rightBottomFront, rightTopFront, rightTopBack))
  // back
  addFace(new GLFace(leftBottomBack, rightBottomBack, rightTopBack, leftTopBack))
  // top
  addFace(new GLFace(leftTopBack, rightTopBack, rightTopFront, leftTopFront))
}

object Cube {
  final val KUBE_BOTTOM = 0
  final val KUBE_FRONT  = 1
  final val KUBE_LEFT   = 2
  final val KUBE_RIGHT  = 3
  final val KUBE_BACK   = 4
  final val KUBE_TOP    = 5
}
