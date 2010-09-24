/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.opengl

import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.opengl.{GLSurfaceView, GLU}
import android.util.Log

class GLRenderer(context: Context) extends AnyRef with GLSurfaceView.Renderer {
  import GLRenderer._  // companion object

  private val cube = new GLCube()

  private var startTime: Long = _
  private var fpsStartTime: Long = _
  private var numFrames: Long = _

  def onSurfaceCreated(gl: GL10, config: EGLConfig) {
    // ...
    var SEE_THRU = true

    startTime = System.currentTimeMillis
    fpsStartTime = startTime
    numFrames = 0

    // Define the lighting
    val lightAmbient = Array(0.2f, 0.2f, 0.2f, 1)
    val lightDiffuse = Array[Float](1, 1, 1, 1)
    val lightPos = Array[Float](1, 1, 1, 1)
    gl glEnable GL10.GL_LIGHTING
    gl glEnable GL10.GL_LIGHT0
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, lightAmbient, 0)
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightDiffuse, 0)
    gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, lightPos, 0)

    // What is the cube made of?
    val matAmbient = Array[Float](1, 1, 1, 1)
    val matDiffuse = Array[Float](1, 1, 1, 1)
    gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT,
          matAmbient, 0)
    gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE,
          matDiffuse, 0)

    // Set up any OpenGL options we need
    gl glEnable GL10.GL_DEPTH_TEST
    gl glDepthFunc GL10.GL_LEQUAL
    gl glEnableClientState GL10.GL_VERTEX_ARRAY

    // Optional: disable dither to boost performance
    // gl.glDisable(GL10.GL_DITHER);
    // ...
    if (SEE_THRU) {
      gl glDisable GL10.GL_DEPTH_TEST
      gl glEnable GL10.GL_BLEND
      gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE)
    }

    // Enable textures
    gl glEnableClientState GL10.GL_TEXTURE_COORD_ARRAY
    gl glEnable GL10.GL_TEXTURE_2D

    // Load the cube's texture from a bitmap
    GLCube.loadTexture(gl, context, R.drawable.android)
  }

  def onSurfaceChanged(gl: GL10, width: Int, height: Int) {
    // ...

    // Define the view frustum
    gl.glViewport(0, 0, width, height)
    gl glMatrixMode GL10.GL_PROJECTION
    gl.glLoadIdentity()
    val ratio = width.toFloat / height
    GLU.gluPerspective(gl, 45.0f, ratio, 1, 100f) 
  }

  def onDrawFrame(gl: GL10) {
    // ...

    // Clear the screen to black
    gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT)

    // Position model so we can see it
    gl glMatrixMode GL10.GL_MODELVIEW
    gl.glLoadIdentity()
    gl.glTranslatef(0, 0, -3.0f)

    // Other drawing commands go here...
      
    // Set rotation angle based on the time
    val elapsed = System.currentTimeMillis - startTime
    gl.glRotatef(elapsed * (30f / 1000f), 0, 1, 0)
    gl.glRotatef(elapsed * (15f / 1000f), 1, 0, 0)

    // Draw the model
    cube draw gl

    // Keep track of number of frames drawn
    numFrames += 1
    val fpsElapsed = System.currentTimeMillis - fpsStartTime
    if (fpsElapsed > 5 * 1000) { // every 5 seconds
      val fps = (numFrames * 1000.0f) / fpsElapsed
      Log.d(TAG, "Frames per second: " + fps + " (" + numFrames
          + " frames in " + fpsElapsed + " ms)")
      fpsStartTime = System.currentTimeMillis
      numFrames = 0
    }  
  }

}
object GLRenderer {
   private val TAG = "GLRenderer"
}
