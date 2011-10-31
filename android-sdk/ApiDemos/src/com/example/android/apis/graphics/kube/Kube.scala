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

import android.app.Activity
import android.os.Bundle
import android.view.Window

import android.opengl.GLSurfaceView

import scala.util.Random

class Kube extends Activity with AnimationCallback {
  import Kube._  // companion object

  private def makeGLWorld(): GLWorld = {
    val world = new GLWorld()

    val one = 0x10000
    val half = 0x08000
    val red = new GLColor(one, 0, 0)
    val green = new GLColor(0, one, 0)
    val blue = new GLColor(0, 0, one)
    val yellow = new GLColor(one, one, 0)
    val orange = new GLColor(one, half, 0)
    val white = new GLColor(one, one, one)
    val black = new GLColor(0, 0, 0)

    // coordinates for our cubes
    val c0 = -1.0f
    val c1 = -0.38f
    val c2 = -0.32f
    val c3 = 0.32f
    val c4 = 0.38f
    val c5 = 1.0f

    // top back, left to right
    mCubes(0)  = new Cube(world, c0, c4, c0, c1, c5, c1)
    mCubes(1)  = new Cube(world, c2, c4, c0, c3, c5, c1)
    mCubes(2)  = new Cube(world, c4, c4, c0, c5, c5, c1)
    // top middle, left to right
    mCubes(3)  = new Cube(world, c0, c4, c2, c1, c5, c3)
    mCubes(4)  = new Cube(world, c2, c4, c2, c3, c5, c3)
    mCubes(5)  = new Cube(world, c4, c4, c2, c5, c5, c3)
    // top front, left to right
    mCubes(6)  = new Cube(world, c0, c4, c4, c1, c5, c5)
    mCubes(7)  = new Cube(world, c2, c4, c4, c3, c5, c5)
    mCubes(8)  = new Cube(world, c4, c4, c4, c5, c5, c5)
    // middle back, left to right
    mCubes(9)  = new Cube(world, c0, c2, c0, c1, c3, c1)
    mCubes(10) = new Cube(world, c2, c2, c0, c3, c3, c1)
    mCubes(11) = new Cube(world, c4, c2, c0, c5, c3, c1)
        // middle middle, left to right
    mCubes(12) = new Cube(world, c0, c2, c2, c1, c3, c3)
    mCubes(13) = null
    mCubes(14) = new Cube(world, c4, c2, c2, c5, c3, c3)
        // middle front, left to right
    mCubes(15) = new Cube(world, c0, c2, c4, c1, c3, c5)
    mCubes(16) = new Cube(world, c2, c2, c4, c3, c3, c5)
    mCubes(17) = new Cube(world, c4, c2, c4, c5, c3, c5)
    // bottom back, left to right
    mCubes(18) = new Cube(world, c0, c0, c0, c1, c1, c1)
    mCubes(19) = new Cube(world, c2, c0, c0, c3, c1, c1)
    mCubes(20) = new Cube(world, c4, c0, c0, c5, c1, c1)
    // bottom middle, left to right
    mCubes(21) = new Cube(world, c0, c0, c2, c1, c1, c3)
    mCubes(22) = new Cube(world, c2, c0, c2, c3, c1, c3)
    mCubes(23) = new Cube(world, c4, c0, c2, c5, c1, c3)
    // bottom front, left to right
    mCubes(24) = new Cube(world, c0, c0, c4, c1, c1, c5)
    mCubes(25) = new Cube(world, c2, c0, c4, c3, c1, c5)
    mCubes(26) = new Cube(world, c4, c0, c4, c5, c1, c5)

    // paint the sides
    // set all faces black by default
    for (i <- 0 until 27; cube = mCubes(i) if cube != null)
      for (j <- 0 until 6)
        cube.setFaceColor(j, black)

    // paint top
    for (i <- 0 until 9)
      mCubes(i).setFaceColor(Cube.KUBE_TOP, orange)
    // paint bottom
    for (i <- 18 until 27)
      mCubes(i).setFaceColor(Cube.KUBE_BOTTOM, red)
    // paint left
    for (i <- 0 until 27 by 3)
      mCubes(i).setFaceColor(Cube.KUBE_LEFT, yellow)
    // paint right
    for (i <- 2 until 27 by 3)
      mCubes(i).setFaceColor(Cube.KUBE_RIGHT, white)
    // paint back
    for (i <- 0 until 27 by 9; j <- 0 until 3)
      mCubes(i + j).setFaceColor(Cube.KUBE_BACK, blue)
    // paint front
    for (i <- 6 until 27 by 9; j <- 0 until 3)
      mCubes(i + j).setFaceColor(Cube.KUBE_FRONT, green)

    for (i <- 0 until 27 if mCubes(i) != null)
      world addShape mCubes(i)

    // initialize our permutation to solved position
    mPermutation = new Array[Int](27)
    for (i <- 0 until mPermutation.length)
      mPermutation(i) = i

    createLayers()
    updateLayers()

    world.generate()

    world
  }

  private def createLayers() {
    mLayers(KUBE_UP     ) = new Layer(Layer.AXIS_Y)
    mLayers(KUBE_DOWN   ) = new Layer(Layer.AXIS_Y)
    mLayers(KUBE_LEFT   ) = new Layer(Layer.AXIS_X)
    mLayers(KUBE_RIGHT  ) = new Layer(Layer.AXIS_X)
    mLayers(KUBE_FRONT  ) = new Layer(Layer.AXIS_Z)
    mLayers(KUBE_BACK   ) = new Layer(Layer.AXIS_Z)
    mLayers(KUBE_MIDDLE ) = new Layer(Layer.AXIS_X)
    mLayers(KUBE_EQUATOR) = new Layer(Layer.AXIS_Y)
    mLayers(KUBE_SIDE   ) = new Layer(Layer.AXIS_Z)
  }

  private def updateLayers() {
    var layer = mLayers(KUBE_UP)
    var shapes = layer.mShapes

    // up layer
    for (i <- 0 until shapes.length)
      shapes(i) = mCubes(mPermutation(i))

    // down layer
    layer = mLayers(KUBE_DOWN)
    shapes = layer.mShapes
    var k = 0
    for (i <- 18 until 27) {
      shapes(k) = mCubes(mPermutation(i))
      k += 1
    }

    // left layer
    layer = mLayers(KUBE_LEFT)
    shapes = layer.mShapes
    k = 0
    for (i <- 0 until 27 by 9; j <- 0 until 9 by 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }

    // right layer
    layer = mLayers(KUBE_RIGHT)
    shapes = layer.mShapes
    k = 0
    for (i <- 2 until 27 by 9; j <- 0 until 9 by 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }

    // front layer
    layer = mLayers(KUBE_FRONT)
    shapes = layer.mShapes
    k = 0
    for (i <- 6 until 27 by 9; j <- 0 until 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }

    // back layer
    layer = mLayers(KUBE_BACK)
    shapes = layer.mShapes
    k = 0
    for (i <- 0 until 27 by 9; j <- 0 until 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }

    // middle layer
    layer = mLayers(KUBE_MIDDLE)
    shapes = layer.mShapes
    k = 0
    for (i <- 1 until 27 by 9; j <- 0 until 9 by 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }

    // equator layer
    layer = mLayers(KUBE_EQUATOR)
    shapes = layer.mShapes
    k = 0
    for (i <- 9 until 18) {
      shapes(k) = mCubes(mPermutation(i))
      k += 1
    }

    // side layer
    layer = mLayers(KUBE_SIDE)
    shapes = layer.mShapes
    k = 0
    for (i <- 3 until 27 by 9; j <- 0 until 3) {
      shapes(k) = mCubes(mPermutation(i + j))
      k += 1
    }
  }

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // We don't need a title either.
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    mView = new GLSurfaceView(getApplication)
    mRenderer = new KubeRenderer(makeGLWorld(), this)
    mView setRenderer mRenderer
    setContentView(mView)
  }

  override protected def onResume() {
    super.onResume()
    mView.onResume()
  }

  override protected def onPause() {
    super.onPause()
    mView.onPause()
  }

  def animate() {
    // change our angle of view
    mRenderer.setAngle(mRenderer.getAngle + 1.2f)

    if (mCurrentLayer == null) {
      val layerID = mRandom.nextInt(9)
      mCurrentLayer = mLayers(layerID)
      mCurrentLayerPermutation = mLayerPermutations(layerID)
      mCurrentLayer.startAnimation()
      var direction = mRandom.nextBoolean()
      var count = mRandom.nextInt(3) + 1

      count = 1
      direction = false
      mCurrentAngle = 0
      val increment = (math.Pi / 50).toFloat
      val deltaAngle = ((math.Pi * count) / 2f).toFloat
      if (direction) {
        mAngleIncrement = increment
        mEndAngle = mCurrentAngle + deltaAngle
      } else {
        mAngleIncrement = -increment
        mEndAngle = mCurrentAngle - deltaAngle
      }
    }

    mCurrentAngle += mAngleIncrement

    if ((mAngleIncrement > 0f && mCurrentAngle >= mEndAngle) ||
        (mAngleIncrement < 0f && mCurrentAngle <= mEndAngle)) {
      mCurrentLayer setAngle mEndAngle
      mCurrentLayer.endAnimation()
      mCurrentLayer = null

      // adjust mPermutation based on the completed layer rotation
      val newPermutation = new Array[Int](27)
      for (i <- 0 until newPermutation.length) {
        newPermutation(i) = mPermutation(mCurrentLayerPermutation(i))
 //     newPermutation(i) = mCurrentLayerPermutation(mPermutation(i))
      }
      mPermutation = newPermutation
      updateLayers()

    } else {
      mCurrentLayer setAngle mCurrentAngle
    }
  }

  private var mView: GLSurfaceView = _
  private var mRenderer: KubeRenderer = _
  private val mCubes = new Array[Cube](27)
  // a Layer for each possible move
  private val mLayers = new Array[Layer](9)

  // current permutation of starting position
  private var mPermutation: Array[Int] = _

  // for random cube movements
  private val mRandom = new Random(System.currentTimeMillis)
  // currently turning layer
  private var mCurrentLayer: Layer = _
  // current and final angle for current Layer animation
  private var mCurrentAngle, mEndAngle: Float = _
  // amount to increment angle
  private var mAngleIncrement: Float = _
  private var mCurrentLayerPermutation: Array[Int] = _
}

object Kube {
  // names for our 9 layers (based on notation from http://www.cubefreak.net/notation.html)
  final val KUBE_UP      = 0
  final val KUBE_DOWN    = 1
  final val KUBE_LEFT    = 2
  final val KUBE_RIGHT   = 3
  final val KUBE_FRONT   = 4
  final val KUBE_BACK    = 5
  final val KUBE_MIDDLE  = 6
  final val KUBE_EQUATOR = 7
  final val KUBE_SIDE    = 8

  // permutations corresponding to a pi/2 rotation of each layer about its axis
  final val mLayerPermutations = Array(
    // permutation for UP layer
    Array( 2, 5, 8, 1, 4, 7, 0, 3, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26),
    // permutation for DOWN layer
    Array( 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 20, 23, 26, 19, 22, 25, 18, 21, 24),
    // permutation for LEFT layer
    Array( 6, 1, 2, 15, 4, 5, 24, 7, 8, 3, 10, 11, 12, 13, 14, 21, 16, 17, 0, 19, 20, 9, 22, 23, 18, 25, 26),
    // permutation for RIGHT layer
    Array( 0, 1, 8, 3, 4, 17, 6, 7, 26, 9, 10, 5, 12, 13, 14, 15, 16, 23, 18, 19, 2, 21, 22, 11, 24, 25, 20),
    // permutation for FRONT layer
    Array( 0, 1, 2, 3, 4, 5, 24, 15, 6, 9, 10, 11, 12, 13, 14, 25, 16, 7, 18, 19, 20, 21, 22, 23, 26, 17, 8),
    // permutation for BACK layer
    Array( 18, 9, 0, 3, 4, 5, 6, 7, 8, 19, 10, 1, 12, 13, 14, 15, 16, 17, 20, 11, 2, 21, 22, 23, 24, 25, 26),
    // permutation for MIDDLE layer
    Array( 0, 7, 2, 3, 16, 5, 6, 25, 8, 9, 4, 11, 12, 13, 14, 15, 22, 17, 18, 1, 20, 21, 10, 23, 24, 19, 26),
    // permutation for EQUATOR layer
    Array( 0, 1, 2, 3, 4, 5, 6, 7, 8, 11, 14, 17, 10, 13, 16, 9, 12, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26),
    // permutation for SIDE layer
    Array( 0, 1, 2, 21, 12, 3, 6, 7, 8, 9, 10, 11, 22, 13, 4, 15, 16, 17, 18, 19, 20, 23, 14, 5, 24, 25, 26)
  )
}
