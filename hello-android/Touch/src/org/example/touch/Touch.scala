/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.touch

import android.app.Activity
import android.graphics.{Matrix, PointF}
import android.os.Bundle
import android.util.{FloatMath, Log}
import android.view.{MotionEvent, View}
import android.view.View.OnTouchListener
import android.widget.ImageView

class Touch extends Activity with OnTouchListener {
  import Touch._  // companion object

  // These matrices will be used to move and zoom image
  private val matrix = new Matrix()
  private val savedMatrix = new Matrix()

  private var mode = NONE

  // Remember some things for zooming
  private val start = new PointF()
  private val mid = new PointF()
  private var oldDist = 1f

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    val view = findViewById(R.id.imageView).asInstanceOf[ImageView]
    view setOnTouchListener this

    // ...
    // Work around a Cupcake bug
    matrix.setTranslate(1f, 1f)
    view setImageMatrix matrix
  }

  override def onTouch(v: View, rawEvent: MotionEvent): Boolean = {
    val event = WrapMotionEvent.wrap(rawEvent)
    // ...
    val view = v.asInstanceOf[ImageView]

    // Dump touch event to log
    dumpEvent(event)

    // Handle touch events here...
    event.getAction & MotionEvent.ACTION_MASK match {
      case MotionEvent.ACTION_DOWN =>
        savedMatrix set matrix
        start.set(event.getX, event.getY)
        mode = DRAG
        Log.d(TAG, "mode=DRAG")

      case MotionEvent.ACTION_POINTER_DOWN =>
        oldDist = spacing(event)
        Log.d(TAG, "oldDist=" + oldDist)
        if (oldDist > 10f) {
          savedMatrix set matrix
          midPoint(mid, event)
          mode = ZOOM
          Log.d(TAG, "mode=ZOOM")
       }

      case MotionEvent.ACTION_UP | MotionEvent.ACTION_POINTER_UP =>
        mode = NONE
        Log.d(TAG, "mode=NONE")

      case MotionEvent.ACTION_MOVE =>
        if (mode == DRAG) {
          // ...
          matrix set savedMatrix
          matrix.postTranslate(event.getX - start.x, event.getY - start.y)
        }
        else if (mode == ZOOM) {
          val newDist = spacing(event)
          Log.d(TAG, "newDist=" + newDist)
          if (newDist > 10f) {
            matrix set savedMatrix
            val scale = newDist / oldDist
            matrix.postScale(scale, scale, mid.x, mid.y)
          }
        }
      }

    view setImageMatrix matrix
    true // indicate event was handled
  }

  /** Show an event in the LogCat view, for debugging */
  private def dumpEvent(event: WrapMotionEvent) {
    // ...
    val names = Array("DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
                      "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?")
    val sb = new StringBuilder()
    val action = event.getAction
    val actionCode = action & MotionEvent.ACTION_MASK
    sb append "event ACTION_" append names(actionCode)
    if (actionCode == MotionEvent.ACTION_POINTER_DOWN
            || actionCode == MotionEvent.ACTION_POINTER_UP) {
      sb append "(pid "
      sb append (action >> MotionEvent.ACTION_POINTER_ID_SHIFT)
      sb append ")"
    }
    sb append "["
    for (i <- 0 until event.getPointerCount) {
      sb append "#" append i
      sb append "(pid " append event.getPointerId(i)
      sb append ")=" append event.getX(i).toInt
      sb append "," append event.getY(i).toInt
      if (i + 1 < event.getPointerCount)
        sb append ";"
    }
    sb append "]"
    Log.d(TAG, sb.toString)
  }

  /** Determine the space between the first two fingers */
  private def spacing(event: WrapMotionEvent): Float = {
    // ...
    val x = event.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)
    FloatMath.sqrt(x * x + y * y)
  }

  /** Calculate the mid point of the first two fingers */
  private def midPoint(point: PointF, event: WrapMotionEvent) {
    // ...
    val x = event.getX(0) + event.getX(1)
    val y = event.getY(0) + event.getY(1)
    point.set(x / 2, y / 2)
  }
}

object Touch {
  private final val TAG = "Touch"

  // We can be in one of these 3 states
  final val NONE = 0
  final val DRAG = 1
  final val ZOOM = 2
}

