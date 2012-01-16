package com.manning.aip.canvasdemo

import android.app.Activity
import android.content.Context
import android.graphics.{Canvas, Paint}
import android.os.Bundle
import android.view.View

import scala.util.Random

class Canvas2DRandomShapesActivity extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(new CanvasView(this))
  }

  class CanvasView(context: Context) extends View(context) {
    private var paint: Paint = _
    private val random = new Random()

    override protected def onDraw(canvas: Canvas) {
      canvas.drawRGB(0, 0, 0)
      for (i <- 0 until 10) {
        paint = new Paint()
        paint.setARGB(255, random nextInt 256, random nextInt 256, random nextInt 256)
        val (w, h) = (canvas.getWidth, canvas.getHeight)
        canvas.drawLine(random nextInt w, random nextInt h,
                        random nextInt w, random nextInt h, paint)
        canvas.drawCircle(random nextInt (w - 30), random nextInt (h - 30),
                          random nextInt 30, paint)
        canvas.drawRect(random nextInt w, random nextInt h,
                        random nextInt w, random nextInt h, paint)
      }
    }
  }
}
