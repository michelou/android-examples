package com.manning.aip.canvasdemo

import android.content.Context
import android.graphics.{Canvas, Color, Paint, Path, Typeface}
import android.view.View

/**
 * View to display a true type font via Canvas.
 * 
 * @author tamas
 * 
 */
class ShapesAndTextFontView(context: Context) extends View(context) {
  private var paint: Paint = _
  private val font = Typeface.createFromAsset(context.getAssets, "256bytes.ttf")
  private var text: String = _

  def setText(text: String) { this.text = text }

  override protected def onDraw(canvas: Canvas) {
    canvas.drawRGB(0, 0, 0)
    drawShapes(canvas)
    drawText(canvas)
  }

  private def drawShapes(canvas: Canvas) {
    // draw a red square, a green circle and blue triangle in the bottom part of the screen
    val side = canvas.getWidth / 5
    paint = new Paint();
    paint.setARGB(255, 255, 0, 0)
    val h = canvas.getHeight - 60
    canvas.drawRect(side, h - side, side + side, h, paint)
    paint.setARGB(255, 0, 255, 0)
    canvas.drawCircle(side * 2 + side / 2, h - side / 2, side / 2, paint)
    paint.setARGB(255, 0, 0, 255)
    paint setStyle Paint.Style.FILL
    val triangle = new Path()
    triangle.moveTo(side * 3 + 30, h - side)
    triangle.lineTo(side * 3 + 60, h)
    triangle.lineTo(side * 3, h)
    triangle.lineTo(side * 3 + 30, h - side)
    canvas.drawPath(triangle, paint)
  }

  private def drawText(canvas: Canvas) {
    paint setColor Color.WHITE
    paint setTextSize 40
    paint setTypeface font
    canvas.drawText(text, 60, 300, paint)
  }
}
