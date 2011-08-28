/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.example.android.basicglsurfaceview

import java.io.{IOException, InputStream}
import java.nio.{ByteBuffer, ByteOrder, FloatBuffer}

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.{GLES20, GLSurfaceView, GLUtils, Matrix}
import android.os.SystemClock
import android.util.Log

class GLES20TriangleRenderer(mContext: Context) extends GLSurfaceView.Renderer {
  import GLES20TriangleRenderer._  // companion object

  private val mTriangleVertices = ByteBuffer.allocateDirect(
    mTriangleVerticesData.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder).asFloatBuffer
  mTriangleVertices.put(mTriangleVerticesData).position(0)

  def onDrawFrame(glUnused: GL10) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.
    GLES20.glClearColor(0.0f, 0.0f, 1.0f, 1.0f)
    GLES20.glClear( GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT)
    GLES20.glUseProgram(mProgram)
    checkGlError("glUseProgram")

    GLES20 glActiveTexture GLES20.GL_TEXTURE0
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)

    mTriangleVertices position TRIANGLE_VERTICES_DATA_POS_OFFSET
    GLES20.glVertexAttribPointer(maPositionHandle, 3, GLES20.GL_FLOAT, false,
      TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices)
    checkGlError("glVertexAttribPointer maPosition")
    mTriangleVertices.position(TRIANGLE_VERTICES_DATA_UV_OFFSET)
    GLES20 glEnableVertexAttribArray maPositionHandle
    checkGlError("glEnableVertexAttribArray maPositionHandle")
    GLES20.glVertexAttribPointer(maTextureHandle, 2, GLES20.GL_FLOAT, false,
      TRIANGLE_VERTICES_DATA_STRIDE_BYTES, mTriangleVertices)
    checkGlError("glVertexAttribPointer maTextureHandle")
    GLES20 glEnableVertexAttribArray maTextureHandle
    checkGlError("glEnableVertexAttribArray maTextureHandle")

    val time = SystemClock.uptimeMillis % 4000L
    val angle = 0.090f * time.toInt
    Matrix.setRotateM(mMMatrix, 0, angle, 0, 0, 1.0f)
    Matrix.multiplyMM(mMVPMatrix, 0, mVMatrix, 0, mMMatrix, 0)
    Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mMVPMatrix, 0)

    GLES20.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, mMVPMatrix, 0)
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    checkGlError("glDrawArrays")
  }

  def onSurfaceChanged(glUnused: GL10, width: Int, height: Int) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.
    GLES20.glViewport(0, 0, width, height)
    val ratio = width.toFloat / height
    Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7)
  }

  def onSurfaceCreated(glUnused: GL10, config: EGLConfig) {
    // Ignore the passed-in GL10 interface, and use the GLES20
    // class's static methods instead.
    mProgram = createProgram(mVertexShader, mFragmentShader)
    if (mProgram == 0)
      return

    maPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition")
    checkGlError("glGetAttribLocation aPosition")
    if (maPositionHandle == -1)
      throw new RuntimeException("Could not get attrib location for aPosition")

    maTextureHandle = GLES20.glGetAttribLocation(mProgram, "aTextureCoord");
    checkGlError("glGetAttribLocation aTextureCoord")
    if (maTextureHandle == -1)
      throw new RuntimeException("Could not get attrib location for aTextureCoord")

    muMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
    checkGlError("glGetUniformLocation uMVPMatrix")
    if (muMVPMatrixHandle == -1)
      throw new RuntimeException("Could not get attrib location for uMVPMatrix")

    /*
     * Create our texture. This has to be done each time the
     * surface is created.
     */

    val textures = new Array[Int](1)
    GLES20.glGenTextures(1, textures, 0)

    mTextureID = textures(0)
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureID)

    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_NEAREST)
    GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR)

    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_REPEAT)
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_REPEAT)

    val is = mContext.getResources openRawResource R.raw.robot
    val bitmap =
      try BitmapFactory decodeStream is
      finally {
        try is.close()
        catch { case e: IOException => /* Ignore */ }
      }

    GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
    bitmap.recycle()

    Matrix.setLookAtM(mVMatrix, 0, 0, 0, -5, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
  }

  private def loadShader(shaderType: Int, source: String): Int = {
    var shader = GLES20 glCreateShader shaderType
    if (shader != 0) {
      GLES20.glShaderSource(shader, source)
      GLES20 glCompileShader shader
      val compiled = new Array[Int](1)
      GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
      if (compiled(0) == 0) {
        Log.e(TAG, "Could not compile shader " + shaderType + ":")
        Log.e(TAG, GLES20.glGetShaderInfoLog(shader))
        GLES20.glDeleteShader(shader)
        shader = 0
      }
    }
    shader
  }

  private def createProgram(vertexSource: String, fragmentSource: String): Int = {
    val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
    if (vertexShader == 0)
      return 0

    val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
    if (pixelShader == 0)
      return 0

    var program = GLES20.glCreateProgram()
    if (program != 0) {
      GLES20.glAttachShader(program, vertexShader)
      checkGlError("glAttachShader")
      GLES20.glAttachShader(program, pixelShader)
      checkGlError("glAttachShader")
      GLES20.glLinkProgram(program)
      val linkStatus = new Array[Int](1)
      GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
      if (linkStatus(0) != GLES20.GL_TRUE) {
        Log.e(TAG, "Could not link program: ")
        Log.e(TAG, GLES20.glGetProgramInfoLog(program))
        GLES20.glDeleteProgram(program)
        program = 0
      }
    }
    program
  }

  private def checkGlError(op: String) {
    var error = GLES20.glGetError
    while (error != GLES20.GL_NO_ERROR) {
      Log.e(TAG, op + ": glError " + error)
      throw new RuntimeException(op + ": glError " + error)
      error = GLES20.glGetError
    }
  }

  private final val mVertexShader =
    "uniform mat4 uMVPMatrix;\n" +
    "attribute vec4 aPosition;\n" +
    "attribute vec2 aTextureCoord;\n" +
    "varying vec2 vTextureCoord;\n" +
    "void main() {\n" +
    "  gl_Position = uMVPMatrix * aPosition;\n" +
    "  vTextureCoord = aTextureCoord;\n" +
    "}\n"

  private final val mFragmentShader =
    "precision mediump float;\n" +
    "varying vec2 vTextureCoord;\n" +
    "uniform sampler2D sTexture;\n" +
    "void main() {\n" +
    "  gl_FragColor = texture2D(sTexture, vTextureCoord);\n" +
    "}\n"

  private val mMVPMatrix = new Array[Float](16)
  private val mProjMatrix = new Array[Float](16)
  private val mMMatrix = new Array[Float](16)
  private val mVMatrix = new Array[Float](16)

  private var mProgram: Int = _
  private var mTextureID: Int = _
  private var muMVPMatrixHandle: Int = _
  private var maPositionHandle: Int = _
  private var maTextureHandle: Int = _
}

object GLES20TriangleRenderer {
  private val TAG = "GLES20TriangleRenderer"
  
  private final val FLOAT_SIZE_BYTES = 4
  private final val TRIANGLE_VERTICES_DATA_STRIDE_BYTES = 5 * FLOAT_SIZE_BYTES
  private final val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
  private final val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
  private final val mTriangleVerticesData = Array(
    // X, Y, Z, U, V
    -1.0f, -0.5f, 0, -0.5f, 0.0f,
    1.0f, -0.5f, 0, 1.5f, -0.0f,
    0.0f,  1.11803399f, 0, 0.5f,  1.61803399f
  )
}
