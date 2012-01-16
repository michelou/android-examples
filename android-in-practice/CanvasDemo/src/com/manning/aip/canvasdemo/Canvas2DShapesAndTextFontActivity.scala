package com.manning.aip.canvasdemo

import android.app.Activity
import android.os.Bundle

class Canvas2DShapesAndTextFontActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val view = new ShapesAndTextFontView(this)
    view setText "256 byte Style"
    setContentView(view)
  }
}
