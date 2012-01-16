package com.manning.aip.canvasdemo

import android.app.Activity
import android.os.Bundle

class Canvas2DShapesAndTextBitmapActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(new ShapesAndTextBitmapView(this))
  }
}
