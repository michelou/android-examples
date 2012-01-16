package com.manning.aip.canvasdemo

import android.app.Activity
import android.os.Bundle

class Canvas2DShapesAndTextLHXStyleActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val view = new ShapesAndTextView(this)
    view setText "LHX Style"
    setContentView(view)
  }
}
