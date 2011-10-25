/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.touch

import android.view.MotionEvent

object WrapMotionEvent {
  def wrap(event: MotionEvent): WrapMotionEvent =
    try
      new EclairMotionEvent(event)
    catch {
      case e: VerifyError =>
      new WrapMotionEvent(event)
    }
}

class WrapMotionEvent protected (event: MotionEvent) {
  //protected var event: MotionEvent = _

  def getAction: Int = event.getAction

  def getX: Float = event.getX

  def getX( pointerIndex: Int): Float = {
    verifyPointerIndex(pointerIndex)
    getX
  }

  def getY: Float = event.getY

  def getY(pointerIndex: Int): Float = {
    verifyPointerIndex(pointerIndex)
    getY
  }

  def getPointerCount: Int = 1

  def getPointerId(pointerIndex: Int): Int = {
    verifyPointerIndex(pointerIndex)
    0
  }

  private def verifyPointerIndex(pointerIndex: Int) {
    if (pointerIndex > 0)
      throw new IllegalArgumentException(
        "Invalid pointer index for Donut/Cupcake")
 }
   
}

