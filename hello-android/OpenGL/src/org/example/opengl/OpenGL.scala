/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/

package org.example.opengl

import android.app.Activity
import android.os.Bundle

class OpenGL extends Activity {
  var view: GLView = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    view = new GLView(this)
    setContentView(view)
  }

  override protected def onPause() {
    super.onPause()
    view.onPause()
  }

  override protected def onResume() {
    super.onResume()
    view.onResume()
  }
}
