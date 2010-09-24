/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class GLView(context: Context) extends GLSurfaceView(context) {
  private val renderer = new GLRenderer(context)
  setRenderer(renderer)

  // Uncomment this to turn on error-checking and logging
  //setDebugFlags(DEBUG_CHECK_GL_ERROR | DEBUG_LOG_GL_CALLS);
}
