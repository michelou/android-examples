/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.android.divideandconquer

import android.content.Context
import android.content.res.Resources
import android.graphics._
import android.graphics.drawable.GradientDrawable
import android.os.{Debug, SystemClock}
import android.util.{AttributeSet, TypedValue}
import android.view.{KeyEvent, MotionEvent, View}

import scala.collection.mutable.ListBuffer

/**
 * Handles the visual display and touch input for the game.
 */
class DivideAndConquerView(context: Context, attrs: AttributeSet)
extends View(context, attrs) with BallEngine.BallEventCallBack {
  import DivideAndConquerView._  // companion object

  private var mDrawingProfilingStarted = false

  private val mPaint = new Paint()
  mPaint setAntiAlias true
  mPaint setStrokeWidth 2
  mPaint setColor Color.BLACK

  private var mEngine: BallEngine = _

  private var mMode = Mode.Paused

  private var mCallback: BallEngineCallBack = _

  // interface for starting a line
  private var mDirectionPoint: DirectionPoint = _
  private val mBallBitmap =
    BitmapFactory.decodeResource(context.getResources, R.drawable.ball)
  private val mBallBitmapRadius =
    mBallBitmap.getWidth.toFloat / 2f
  private val mExplosion1 =
    BitmapFactory.decodeResource(context.getResources, R.drawable.explosion1)
  private val mExplosion2 =
    BitmapFactory.decodeResource(context.getResources, R.drawable.explosion2)
  private val mExplosion3 =
    BitmapFactory.decodeResource(context.getResources, R.drawable.explosion3)

  // so we can see the back key
  setFocusableInTouchMode(true)

  drawBackgroundGradient()

  /**
   * @return The ball engine associated with the game.
   */
  def getEngine: BallEngine = mEngine

  private val mBackgroundGradient = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    Array(Color.RED, Color.YELLOW))

  private def drawBackgroundGradient() {
    setBackgroundDrawable(mBackgroundGradient)
  }

  /**
   * Set the callback that will be notified of events related to the ball
   * engine.
   * @param callback The callback.
   */
  def setCallback(callback: BallEngineCallBack) {
    mCallback = callback
  }

  override protected def onSizeChanged(i: Int, i1: Int, i2: Int, i3: Int) {
    super.onSizeChanged(i, i1, i2, i3)

    // this should only happen once when the activity is first launched.
    // we could be smarter about saving / restoring across activity
    // lifecycles, but for now, this is good enough to handle in game play,
    // and most cases of navigating away with the home key and coming back.
    mEngine = new BallEngine(
                BORDER_WIDTH, getWidth - BORDER_WIDTH,
                BORDER_WIDTH, getHeight - BORDER_WIDTH,
                BALL_SPEED,
                BALL_RADIUS)
    mEngine setCallBack this
    mCallback onEngineReady mEngine
  }

  /**
   * @return the current mode of operation.
   */
  def getMode: Mode = mMode

  /**
   * Set the mode of operation.
   * @param mode The mode.
   */
  def setMode(mode: Mode) {
    mMode = mode

    if (mMode == Mode.Bouncing && mEngine != null) {
      // when starting up again, the engine needs to know what 'now' is.
      val now = SystemClock.elapsedRealtime
      mEngine setNow now

      mExplosions.clear()
      invalidate()
    }
  }

  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    // the first time the user hits back while the balls are moving,
    // we'll pause the game.  but if they hit back again, we'll do the usual
    // (exit the activity)
    if (keyCode == KeyEvent.KEYCODE_BACK && mMode == Mode.Bouncing) {
      setMode(Mode.PausedByUser)
      true
    } else
      super.onKeyDown(keyCode, event)
  }

  override def onTouchEvent(motionEvent: MotionEvent): Boolean = {
    if (mMode == Mode.PausedByUser) {
      // touching unpauses when the game was paused by the user.
      setMode(Mode.Bouncing)
      return true
    } else if (mMode == Mode.Paused) {
      return false
    }

    val x = motionEvent.getX
    val y = motionEvent.getY
    motionEvent.getAction match {
      case MotionEvent.ACTION_DOWN =>
        if (mEngine.canStartLineAt(x, y)) {
          mDirectionPoint = new DirectionPoint(x, y)
        }
        true
      case MotionEvent.ACTION_MOVE =>
        if (mDirectionPoint != null) {
          mDirectionPoint.updateEndPoint(x, y)
        } else if (mEngine.canStartLineAt(x, y)) {
          mDirectionPoint = new DirectionPoint(x, y)
        }
        true
      case MotionEvent.ACTION_UP if mDirectionPoint != null =>
        mDirectionPoint.getDirection match {
          case AmbiguousDirection.Unknown =>
            // do nothing
          case AmbiguousDirection.Horizonal =>
            mEngine.startHorizontalLine(SystemClock.elapsedRealtime,
                                    mDirectionPoint.getX, mDirectionPoint.getY)
            if (PROFILE_DRAWING) {
              if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("BallsDrawing")
                mDrawingProfilingStarted = true
              }
            }
          case AmbiguousDirection.Vertical =>
            mEngine.startVerticalLine(SystemClock.elapsedRealtime,
                                    mDirectionPoint.getX, mDirectionPoint.getY)
            if (PROFILE_DRAWING) {
              if (!mDrawingProfilingStarted) {
                Debug.startMethodTracing("BallsDrawing")
                mDrawingProfilingStarted = true
              }
            }
          case _ =>
        }
        mDirectionPoint = null
        true
      case MotionEvent.ACTION_CANCEL =>
        mDirectionPoint = null
        true
      case _ =>
        false
    }
  }

  /** {@inheritDoc} */
  def onBallHitsBall(ballA: Ball, ballB: Ball) {

  }

  /** {@inheritDoc} */
  def onBallHitsLine(when: Long, ball: Ball, animatingLine: AnimatingLine) {
    mCallback.onBallHitsMovingLine(mEngine, ball.getX, ball.getY)

    mExplosions += new Explosion(when, ball.getX, ball.getY,
                                 mExplosion1, mExplosion2, mExplosion3)
  }

  private val mExplosions = new ListBuffer[Explosion]()

  override protected def onDraw(canvas: Canvas) {
    var newRegion = false

    if (mMode == Mode.Bouncing) {

      // handle the ball engine
      val now = SystemClock.elapsedRealtime
      newRegion = mEngine update now

      if (newRegion) {
        mCallback onAreaChange mEngine

        // reset back to full alpha bg color
        drawBackgroundGradient()
      }

      if (PROFILE_DRAWING) {
        if (newRegion && mDrawingProfilingStarted) {
          mDrawingProfilingStarted = false
          Debug.stopMethodTracing()
        }
      }

      // the X-plosions
      for (explosion <- mExplosions) explosion update now
    }

    for (region <- mEngine.getRegions) drawRegion(canvas, region)

    for (explosion <- mExplosions) {
      explosion.draw(canvas, mPaint)
      // TODO prune explosions that are done
    }

    if (mMode == Mode.PausedByUser) {
      drawPausedText(canvas)
    } else if (mMode == Mode.Bouncing) {
      // keep em' bouncing!
      invalidate()
    }
  }

  /**
   * Pain the text instructing the user how to unpause the game.
   */
  private def drawPausedText(canvas: Canvas) {
    mPaint setColor Color.BLACK
    mPaint setAntiAlias true
    mPaint setTextSize TypedValue.applyDimension(
                         TypedValue.COMPLEX_UNIT_SP,
                         20,
                         getResources.getDisplayMetrics)
    val unpauseInstructions = getContext.getString(R.string.unpause_instructions)
    canvas.drawText(unpauseInstructions, getWidth / 5, getHeight / 2, mPaint)
    mPaint setAntiAlias false
  }

  private val mRectF = new RectF()

  /**
   * Draw a ball region.
   */
  private def drawRegion(canvas: Canvas, region: BallRegion) {
    // draw fill rect to offset against background
    mPaint setColor Color.LTGRAY

    mRectF.set(region.getLeft, region.getTop,
                region.getRight, region.getBottom)
    canvas.drawRect(mRectF, mPaint)

    //draw an outline
    mPaint setStyle Paint.Style.STROKE
    mPaint setColor Color.WHITE
    canvas.drawRect(mRectF, mPaint)
    mPaint setStyle Paint.Style.FILL  // restore style

    // draw each ball
    for (ball <- region.getBalls) {
      // canvas.drawCircle(ball.getX, ball.getY, BALL_RADIUS, mPaint)
      canvas.drawBitmap(mBallBitmap,
                        ball.getX - mBallBitmapRadius,
                        ball.getY - mBallBitmapRadius,
                        mPaint)
    }

    // draw the animating line
    val al = region.getAnimatingLine
    if (al != null) {
      drawAnimatingLine(canvas, al)
    }
  }

  /**
   * Draw an animating line.
   */
  private def drawAnimatingLine(canvas: Canvas, al: AnimatingLine) {
    val perc = al.getPercentageDone
    val color = Color.RED
    mPaint setColor Color.argb(0xFF,
                               scaleToBlack(Color.red(color), perc),
                               scaleToBlack(Color.green(color), perc),
                               scaleToBlack(Color.blue(color), perc))
    al.getDirection match {
      case Direction.Horizontal =>
        canvas.drawLine(al.getStart, al.getPerpAxisOffset,
                        al.getEnd, al.getPerpAxisOffset,
                        mPaint)
      case Direction.Vertical =>
        canvas.drawLine(al.getPerpAxisOffset, al.getStart,
                        al.getPerpAxisOffset, al.getEnd,
                        mPaint)
    }
  }
}

object DivideAndConquerView {
  final val BORDER_WIDTH = 10

  // this needs to match size of ball drawable
  final val BALL_RADIUS = 5f

  final val BALL_SPEED = 80f

  // if true, will profile the drawing code during each animating line and export
  // the result to a file named 'BallsDrawing.trace' on the sd card
  // this file can be pulled off and profiled with traceview
  // $ adb pull /sdcard/BallsDrawing.trace .
  // traceview BallsDrawing.trace
  private final val PROFILE_DRAWING = false

  /**
   * Callback notifying of events related to the ball engine.
   */
  trait BallEngineCallBack {

    /**
     * The engine has its dimensions and is ready to go.
     * @param ballEngine The ball engine.
     */
    def onEngineReady(ballEngine: BallEngine)

    /**
     * A ball has hit a moving line.
     * @param ballEngine The engine.
     * @param x The x coordinate of the ball.
     * @param y The y coordinate of the ball.
     */
    def onBallHitsMovingLine(ballEngine: BallEngine, x: Float, y: Float)

    /**
     * A line made it to the edges of its region, splitting off a new region.
     * @param ballEngine The engine.
     */
    def onAreaChange(ballEngine: BallEngine)
  }

  /**
   * Keeps track of the mode of this view.
   */
  object Mode extends Enumeration {

    val
    /**
     * The balls are bouncing around.
     */
    Bouncing,

    /**
     * The animation has stopped and the balls won't move around.  The user
     * may not unpause it; this is used to temporarily stop games between
     * levels, or when the game is over and the activity places a dialog up.
     */
    Paused,

    /**
     * Same as {@link #Paused}, but paints the word 'touch to unpause' on
     * the screen, so the user knows he/she can unpause the game.
     */
    PausedByUser = Value
  }
  type Mode = Mode.Value

  class Explosion(lastUpdate: Long, mX: Float, mY: Float,
                  explosion1: Bitmap, explosion2: Bitmap, explosion3: Bitmap) {
    private var mLastUpdate: Long = lastUpdate
    private var mProgress: Long = 0
    private val mRadius = explosion1.getWidth.toFloat / 2f

    def update(now: Long) {
      mProgress += (now - mLastUpdate)
      mLastUpdate = now
    }

    def setNow(now: Long) { mLastUpdate = now }

    def draw(canvas: Canvas, paint: Paint) {
      if (mProgress < 80L)
        canvas.drawBitmap(explosion1, mX - mRadius, mY - mRadius, paint)
      else if (mProgress < 160L)
        canvas.drawBitmap(explosion2, mX - mRadius, mY - mRadius, paint)
      else if (mProgress < 400L)
        canvas.drawBitmap(explosion3, mX - mRadius, mY - mRadius, paint)
    }

    def done: Boolean = mProgress > 700L
  }

  private def scaleToBlack(component: Int, percentage: Float): Int = {
    // ((1f - percentage*0.4f) * component).toInt
    (percentage * 0.6f * (0xFF - component) + component).toInt
  }
}
