package com.manning.aip.canvasdemo

import android.content.Context
import android.content.res.TypedArray
import android.graphics.{Canvas, Color, CornerPathEffect, LinearGradient, Paint}
import android.graphics.Paint.{Align, Style}
import android.graphics.{PathEffect, Rect, Shader, Typeface}
import android.util.AttributeSet
import android.view.View

class CustomButton(context: Context, attrs: AttributeSet, defStyle: Int)
extends View(context, attrs, defStyle) {

  private val borderRadius = new CornerPathEffect(5)

  private val borderPaint = new Paint()
    borderPaint setStyle Style.STROKE
    borderPaint setColor Color.rgb(75, 75, 75)
    borderPaint setPathEffect borderRadius
    borderPaint setStrokeWidth 2F
    borderPaint setAntiAlias true

  private val textPaint = new Paint()
    textPaint.setShadowLayer(1.0F, 0F, 2F, Color.WHITE)
    textPaint setTextAlign Align.CENTER
    textPaint setColor Color.BLACK
    textPaint setStyle Style.FILL
    textPaint setAntiAlias true
    textPaint setTypeface Typeface.SANS_SERIF

  private val countPaint = new Paint()
    countPaint.setShadowLayer(1.0F, 0F, 2F, Color.WHITE)
    countPaint setTextAlign Align.RIGHT
    countPaint setColor Color.BLUE
    countPaint setStyle Style.FILL
    countPaint setTypeface Typeface.MONOSPACE
    countPaint setAntiAlias true

  private val squarePaint = new Paint()
    squarePaint setStyle Style.FILL
    squarePaint setColor Color.rgb(245, 245, 245)
    squarePaint setPathEffect borderRadius
    squarePaint setAntiAlias true

  private var height: Int = _
  private var width: Int = _

  private var count = 0
  private var text = "Button"

  if (attrs != null) {
    val a: TypedArray = getContext.obtainStyledAttributes(attrs, R.styleable.CustomButton, 0, 0)
    val attText = a getString R.styleable.CustomButton_text
    val attCount = a.getInt(R.styleable.CustomButton_count, 0)
    if (attText != null) text = attText
    if (attCount > 0) count = attCount
    a.recycle()
  }

  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null)

  def setCount(count: Int) { this.count = count }
  def getCount: Int = this.count

  def setText(text: String) { this.text = text }
  def getText: String = this.text

  override def onDraw(canvas: Canvas) {
    squarePaint setShader new LinearGradient(0F, 0F, 0F, height, Color.rgb(254, 254, 254), Color.rgb(221, 221, 221),
               Shader.TileMode.REPEAT)

    textPaint.setTextSize(width * 0.09F)

    countPaint.setTextSize(height * 0.3F)

    val rect = new Rect(0, 0, width, height)
    canvas.drawRect(rect, squarePaint)
    canvas.drawText(text, (width / 2) - (width / 10) + 10, (height / 2) + (height / 3), textPaint)
    canvas.drawText("" + count, (width * 0.92).toInt, height / 3, countPaint)
  }

  override protected def onMeasure(widthSpecId: Int, heightSpecId: Int) {
    this.height = View.MeasureSpec getSize heightSpecId
    this.width = View.MeasureSpec getSize widthSpecId
    setMeasuredDimension(this.width, this.height)
  }
}
