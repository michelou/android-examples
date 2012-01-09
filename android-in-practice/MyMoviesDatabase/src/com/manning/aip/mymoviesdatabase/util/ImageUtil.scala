package com.manning.aip.mymoviesdatabase.util

import android.graphics.{Bitmap, Canvas, Paint, PorterDuff, PorterDuffXfermode, Rect, RectF}
import android.graphics.Bitmap.Config

object ImageUtil {
  private final val COLOR = 0xff424242
   
  // source: http://stackoverflow.com/questions/2459916/how-to-make-an-imageview-to-have-rounded-corners
  def getRoundedCornerBitmap(bitmap: Bitmap, roundPx: Int): Bitmap = {
    val output = Bitmap.createBitmap(bitmap.getWidth, bitmap.getHeight, Config.ARGB_8888)
    val canvas = new Canvas(output)

    val paint = new Paint()
    val rect = new Rect(0, 0, bitmap.getWidth, bitmap.getHeight)
    val rectF = new RectF(rect)

    paint setAntiAlias true
    canvas.drawARGB(0, 0, 0, 0)
    paint setColor COLOR
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint)

    paint setXfermode new PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(bitmap, rect, rect, paint)

    output
  }
}
