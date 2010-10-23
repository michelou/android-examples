/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.tictactoe.library

import scala.util.Random

import android.content.Context
import android.content.res.Resources
import android.graphics.{Bitmap, BitmapFactory, Canvas, Paint, Rect}
import android.graphics.Bitmap.Config
import android.graphics.BitmapFactory.Options
import android.graphics.Paint.Style
import android.graphics.drawable.Drawable
import android.os.{Bundle, Handler, Message, Parcelable}
import android.os.Handler.Callback
import android.util.AttributeSet
import android.view.{MotionEvent, View}
import android.view.View.MeasureSpec

class GameView(context: Context, attrs: AttributeSet) extends View(context, attrs) {
  import GameView._  // companion object

  private final val mHandler = new Handler(new MyHandler())

  private final val mSrcRect = new Rect()
  private final val mDstRect = new Rect()

  private var mSxy: Int = _
  private var mOffetX: Int = _
  private var mOffetY: Int = _
  private var mWinPaint: Paint = _
  private var mLinePaint: Paint = _
  private var mBmpPaint: Paint = _

  private var mCellListener: ICellListener = _

  /** Contains one of {@link State#EMPTY}, {@link State#PLAYER1} or {@link State#PLAYER2}. */
  private final val mData = new Array[State](9)

  private var mSelectedCell = -1
  private var mSelectedValue = State.EMPTY
  private var mCurrentPlayer = State.UNKNOWN
  private var mWinner = State.EMPTY

  private var mWinCol = -1;
  private var mWinRow = -1;
  private var mWinDiag = -1;

  private var mBlinkDisplayOff: Boolean = _
  private final var mBlinkRect = new Rect()

  // init
  requestFocus()

  private val mDrawableBg = getResources.getDrawable(R.drawable.lib_bg)
  setBackgroundDrawable(mDrawableBg)

  private val mBmpPlayer1 = getResBitmap(R.drawable.lib_cross)
  private val mBmpPlayer2 = getResBitmap(R.drawable.lib_circle)

  if (mBmpPlayer1 != null) {
    mSrcRect.set(0, 0, mBmpPlayer1.getWidth -1, mBmpPlayer1.getHeight - 1)
  }

  mBmpPaint = new Paint(Paint.ANTI_ALIAS_FLAG)

  mLinePaint = new Paint()
  mLinePaint.setColor(0xFFFFFFFF)
  mLinePaint.setStrokeWidth(5)
  mLinePaint.setStyle(Style.STROKE)

  mWinPaint = new Paint(Paint.ANTI_ALIAS_FLAG)
  mWinPaint setColor 0xFFFF0000
  mWinPaint setStrokeWidth 10
  mWinPaint setStyle Style.STROKE

  if (isInEditMode) {
    // In edit mode (e.g. in the Eclipse ADT graphical layout editor)
    // we'll use some random data to display the state.
    val rnd = new Random()
    for (i <- 0 until mData.length) {
      mData(i) = State.fromInt(rnd.nextInt(3))
    }
  } else {
    for (i <- 0 until mData.length) {
      mData(i) = State.EMPTY
    }
  }

  def getData: Array[State] = mData

  def setCell(cellIndex: Int, value: State) {
    mData(cellIndex) = value
    invalidate()
  }

  def setCellListener(cellListener: ICellListener) {
    mCellListener = cellListener
  }

  def getSelection: Int =
    if (mSelectedValue == mCurrentPlayer) mSelectedCell else -1

  def getCurrentPlayer: State = mCurrentPlayer

  def setCurrentPlayer(player: State) {
    mCurrentPlayer = player
    mSelectedCell = -1
  }

  def getWinner: State = mWinner

  def setWinner(winner: State) {
    mWinner = winner
  }

  /** Sets winning mark on specified column or row (0..2) or diagonal (0..1). */
  def setFinished(col: Int, row: Int, diagonal: Int) {
    mWinCol = col
    mWinRow = row
    mWinDiag = diagonal
  }

  //-----------------------------------------

  override protected def onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val sxy = mSxy
    val s3  = sxy * 3
    val x7 = mOffetX
    val y7 = mOffetY

    var k = sxy
    for (i <- 0 until 2) {
      canvas.drawLine(x7    , y7 + k, x7 + s3 - 1, y7 + k     , mLinePaint)
      canvas.drawLine(x7 + k, y7    , x7 + k     , y7 + s3 - 1, mLinePaint)
      k += sxy
    }

    k = 0
    var y = y7
    for (j <- 0 until 3) {
      var x = x7
      var continue = true
      for (i <- 0 until 3 if continue) {
        mDstRect.offsetTo(MARGIN+x, MARGIN+y)

        val state =
          if (mSelectedCell == k) {
            if (mBlinkDisplayOff) continue = false
            mSelectedValue
          } else {
            mData(k)
          }
        if (continue) {
          state match {
            case State.PLAYER1 if mBmpPlayer1 != null =>
              canvas.drawBitmap(mBmpPlayer1, mSrcRect, mDstRect, mBmpPaint)
            case State.PLAYER2 if mBmpPlayer2 != null =>
              canvas.drawBitmap(mBmpPlayer2, mSrcRect, mDstRect, mBmpPaint)
            case _ =>
          }
          k += 1
          x += sxy
        }
      }
      y += sxy
    }

    if (mWinRow >= 0) {
      val y = y7 + mWinRow * sxy + sxy / 2
      canvas.drawLine(x7 + MARGIN, y, x7 + s3 - 1 - MARGIN, y, mWinPaint)

    } else if (mWinCol >= 0) {
      val x = x7 + mWinCol * sxy + sxy / 2
      canvas.drawLine(x, y7 + MARGIN, x, y7 + s3 - 1 - MARGIN, mWinPaint)

    } else if (mWinDiag == 0) {
      // diagonal 0 is from (0,0) to (2,2)
      canvas.drawLine(x7 + MARGIN, y7 + MARGIN,
             x7 + s3 - 1 - MARGIN, y7 + s3 - 1 - MARGIN, mWinPaint)

    } else if (mWinDiag == 1) {
      // diagonal 1 is from (0,2) to (2,0)
      canvas.drawLine(x7 + MARGIN, y7 + s3 - 1 - MARGIN,
             x7 + s3 - 1 - MARGIN, y7 + MARGIN, mWinPaint)
    }
  }

  override protected def onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    // Keep the view squared
    val w = MeasureSpec.getSize(widthMeasureSpec)
    val h = MeasureSpec.getSize(heightMeasureSpec)
    val d = if (w == 0) h else if (h == 0) w else if (w < h) w else h
    setMeasuredDimension(d, d)
  }

  override protected def onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)

    val sx = (w - 2 * MARGIN) / 3
    val sy = (h - 2 * MARGIN) / 3

    val size = if (sx < sy) sx else sy

    mSxy = size
    mOffetX = (w - 3 * size) / 2
    mOffetY = (h - 3 * size) / 2

    mDstRect.set(MARGIN, MARGIN, size - MARGIN, size - MARGIN)
  }

  override def onTouchEvent(event: MotionEvent): Boolean = {
    val action = event.getAction

    if (action == MotionEvent.ACTION_DOWN) {
      return true

    } else if (action == MotionEvent.ACTION_UP) {
      val sxy = mSxy
      val x = (event.getX.toInt - MARGIN) / sxy
      val y = (event.getY.toInt - MARGIN) / sxy

      if (isEnabled() && x >= 0 && x < 3 && y >= 0 & y < 3) {
        val cell = x + 3 * y

        var state = if (cell == mSelectedCell) mSelectedValue else mData(cell)
        state = if (state == State.EMPTY) mCurrentPlayer else State.EMPTY

        stopBlink()

        mSelectedCell = cell
        mSelectedValue = state
        mBlinkDisplayOff = false
        mBlinkRect.set(MARGIN + x * sxy, MARGIN + y * sxy,
                       MARGIN + (x + 1) * sxy, MARGIN + (y + 1) * sxy)

        if (state != State.EMPTY) {
          // Start the blinker
          mHandler.sendEmptyMessageDelayed(MSG_BLINK, FPS_MS)
        }

        if (mCellListener != null) {
          mCellListener.onCellSelected()
        }
      }
      true
    } else
      false
  }

  def stopBlink() {
    val hadSelection = mSelectedCell != -1 && mSelectedValue != State.EMPTY
    mSelectedCell = -1
    mSelectedValue = State.EMPTY
    if (!mBlinkRect.isEmpty) {
      invalidate(mBlinkRect)
    }
    mBlinkDisplayOff = false
    mBlinkRect.setEmpty()
    mHandler removeMessages MSG_BLINK
    if (hadSelection && mCellListener != null) {
      mCellListener.onCellSelected()
    }
  }

  override protected def onSaveInstanceState(): Parcelable = {
    val b = new Bundle()

    val s = super.onSaveInstanceState()
    b.putParcelable("gv_super_state", s)

    b.putBoolean("gv_en", isEnabled)

    val data = new Array[Int](mData.length)
    for (i <- 0 until data.length) {
      data(i) = mData(i).id
    }
    b.putIntArray("gv_data", data)

    b.putInt("gv_sel_cell", mSelectedCell)
    b.putInt("gv_sel_val",  mSelectedValue.id)
    b.putInt("gv_curr_play", mCurrentPlayer.id)
    b.putInt("gv_winner", mWinner.id)

    b.putInt("gv_win_col", mWinCol)
    b.putInt("gv_win_row", mWinRow)
    b.putInt("gv_win_diag", mWinDiag)

    b.putBoolean("gv_blink_off", mBlinkDisplayOff)
    b.putParcelable("gv_blink_rect", mBlinkRect)

    b
  }

  override protected def onRestoreInstanceState(state: Parcelable) {
    if (!state.isInstanceOf[Bundle]) {
      // Not supposed to happen.
      super.onRestoreInstanceState(state)
      return
    }

    val b = state.asInstanceOf[Bundle]
    val superState = b.getParcelable("gv_super_state")

    setEnabled(b.getBoolean("gv_en", true))

    val data = b.getIntArray("gv_data")
    if (data != null && data.length == mData.length) {
      for (i <- 0 until data.length) {
        mData(i) = State.fromInt(data(i))
      }
    }

    mSelectedCell = b.getInt("gv_sel_cell", -1)
    mSelectedValue = State.fromInt(b.getInt("gv_sel_val", State.EMPTY.id))
    mCurrentPlayer = State.fromInt(b.getInt("gv_curr_play", State.EMPTY.id))
    mWinner = State.fromInt(b.getInt("gv_winner", State.EMPTY.id))

    mWinCol = b.getInt("gv_win_col", -1)
    mWinRow = b.getInt("gv_win_row", -1)
    mWinDiag = b.getInt("gv_win_diag", -1)

    mBlinkDisplayOff = b.getBoolean("gv_blink_off", false)
    val r: Rect = b.getParcelable("gv_blink_rect")
    if (r != null) {
      mBlinkRect set r
    }

    // let the blink handler decide if it should blink or not
    mHandler sendEmptyMessage MSG_BLINK

    super.onRestoreInstanceState(superState)
  }

  //-----

  private class MyHandler extends Callback {
    def handleMessage(msg: Message): Boolean = {
      if (msg.what == MSG_BLINK) {
        if (mSelectedCell >= 0 && mSelectedValue != State.EMPTY && mBlinkRect.top != 0) {
          mBlinkDisplayOff = !mBlinkDisplayOff
          invalidate(mBlinkRect)

          if (!mHandler.hasMessages(MSG_BLINK)) {
            mHandler.sendEmptyMessageDelayed(MSG_BLINK, FPS_MS)
          }
        }
        true
      } else
        false
    }
  }

  private def getResBitmap(bmpResId: Int): Bitmap = {
    val opts = new Options()
    opts.inDither = false

    val res = getResources
    var bmp = BitmapFactory.decodeResource(res, bmpResId, opts)

    if (bmp == null && isInEditMode) {
      // BitmapFactory.decodeResource doesn't work from the rendering
      // library in Eclipse's Graphical Layout Editor. Use this workaround instead.

      val d = res getDrawable bmpResId
      val w = d.getIntrinsicWidth
      val h = d.getIntrinsicHeight
      bmp = Bitmap.createBitmap(w, h, Config.ARGB_8888)
      val c = new Canvas(bmp)
      d.setBounds(0, 0, w - 1, h - 1)
      d draw c
    }

    bmp
  }
}

object GameView {
  final val FPS_MS = 1000L/2

  object State extends Enumeration {
    val UNKNOWN = Value(/*-3,*/ "UNKNOWN")
    val WIN     = Value(/*-2,*/ "WIN")
    val EMPTY   = Value(/* 0,*/ "EMPTY")
    val PLAYER1 = Value(/* 1,*/ "PLAYER1")
    val PLAYER2 = Value(/* 2,*/ "PLAYER2")

    def fromInt(id: Int) = values find (_.id == id) match {
      case Some(v) => v
      case None => UNKNOWN
    }
  }
  type State = State.Value

  trait ICellListener {
    def onCellSelected()
  }

  private final val MARGIN = 4
  private final val MSG_BLINK = 1
}
