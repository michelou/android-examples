/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.opengl

import java.nio.{ByteBuffer, ByteOrder, IntBuffer}

import javax.microedition.khronos.opengles.GL10

import android.content.Context
import android.graphics.{Bitmap, BitmapFactory}
import android.opengl.GLUtils

class GLCube {
  import GLCube._  // companion object

  def draw(gl: GL10) { 
    gl.glVertexPointer(3, GL10.GL_FIXED, 0, mVertexBuffer)

    gl.glEnable(GL10.GL_TEXTURE_2D); // workaround bug 3623
    gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, mTextureBuffer)

    gl.glColor4f(1, 1, 1, 1)
    gl.glNormal3f(0, 0, 1)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4)
    gl.glNormal3f(0, 0, -1)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 4, 4)

    gl.glColor4f(1, 1, 1, 1)
    gl.glNormal3f(-1, 0, 0)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 8, 4)
    gl.glNormal3f(1, 0, 0)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 12, 4)

    gl.glColor4f(1, 1, 1, 1)
    gl.glNormal3f(0, 1, 0)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 16, 4)
    gl.glNormal3f(0, -1, 0)
    gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 20, 4)
  }

}

object GLCube {

  private val mVertexBuffer: IntBuffer = {
    val one = 65536
    val half = one / 2
    val vertices = Array( 
      // FRONT
      -half, -half, half, half, -half, half,
      -half, half, half, half, half, half,
      // BACK
      -half, -half, -half, -half, half, -half,
       half, -half, -half, half, half, -half,
      // LEFT
      -half, -half, half, -half, half, half,
      -half, -half, -half, -half, half, -half,
      // RIGHT
       half, -half, -half, half, half, -half,
       half, -half, half, half, half, half,
      // TOP
      -half, half, half, half, half, half,
      -half, half, -half, half, half, -half,
      // BOTTOM
      -half, -half, half, -half, -half, -half,
       half, -half, half, half, -half, -half
    )
    // Buffers to be passed to gl*Pointer() functions must be
    // direct, i.e., they must be placed on the native heap
    // where the garbage collector cannot move them.
    //
    // Buffers with multi-byte data types (e.g., short, int,
    // float) must have their byte order set to native order
    val vbb = ByteBuffer.allocateDirect(vertices.length * 4)
    vbb order ByteOrder.nativeOrder
    val buf = vbb.asIntBuffer()
    buf put vertices
    buf position 0
    buf
  }

  private val mTextureBuffer: IntBuffer = {
    val one = 65536
    val texCoords = Array(
      // FRONT
      0, one, one, one, 0, 0, one, 0,
      // BACK
      one, one, one, 0, 0, one, 0, 0,
      // LEFT
      one, one, one, 0, 0, one, 0, 0,
      // RIGHT
      one, one, one, 0, 0, one, 0, 0,
      // TOP
      one, 0, 0, 0, one, one, 0, one,
      // BOTTOM
      0, 0, 0, one, one, 0, one, one
    )
    // ...
    val tbb = ByteBuffer.allocateDirect(texCoords.length * 4)
    tbb order ByteOrder.nativeOrder
    val buf = tbb.asIntBuffer()
    buf put texCoords
    buf position 0
    buf
  }

  def loadTexture(gl: GL10, context: Context, resource: Int) {
    val bmp =
      BitmapFactory.decodeResource(context.getResources, resource)
    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bmp, 0)
    gl.glTexParameterx(GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR)
    gl.glTexParameterx(GL10.GL_TEXTURE_2D,
            GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR)
    bmp.recycle()
  }

}
