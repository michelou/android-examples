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

class EclairMotionEvent protected[touch](event: MotionEvent) extends WrapMotionEvent(event) {

  override def getX(pointerIndex: Int): Float = event getX pointerIndex

  override def getY(pointerIndex: Int): Float = event getY pointerIndex

  override def getPointerCount: Int = event.getPointerCount

  override def getPointerId(pointerIndex: Int): Int = event getPointerId pointerIndex

}
