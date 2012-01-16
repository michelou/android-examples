package com.manning.aip.canvasdemo

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.View

import scala.util.Random

class Canvas2DRandomColorActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(new CanvasView(this))
  }

  class CanvasView(context: Context) extends View(context) {
    private val random = new Random()

    override protected def onDraw(canvas: Canvas) {
      canvas.drawRGB(random nextInt 256, random nextInt 256, random nextInt 256)
    }
  }
}
