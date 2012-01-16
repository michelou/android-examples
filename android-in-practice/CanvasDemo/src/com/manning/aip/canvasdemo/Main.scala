package com.manning.aip.canvasdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

class Main extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    def setClick(id: Int, cls: Class[_ <: Activity]) {
      findViewById(id) setOnClickListener new OnClickListener() {
        override def onClick(v: View) {
          startActivity(new Intent(Main.this, cls))
        }
      }
    }

    setClick(R.id.randcolor_button,            classOf[Canvas2DRandomColorActivity])
    setClick(R.id.randcolor_fullscreen_button, classOf[Canvas2DRandomColorFullScreenActivity])
    setClick(R.id.randomshapes_button,         classOf[Canvas2DRandomShapesActivity])
    setClick(R.id.randomshapes_alpha_button,   classOf[Canvas2DRandomShapesWithAlphaActivity])
    setClick(R.id.randomshapes_redraw_button,  classOf[Canvas2DRandomShapesRedrawActivity])
    setClick(R.id.shapesandtext_lhx_button,    classOf[Canvas2DShapesAndTextLHXStyleActivity])
    setClick(R.id.shapesandtext_font_button,   classOf[Canvas2DShapesAndTextFontActivity])
    setClick(R.id.shapesandtext_bitmap_button, classOf[Canvas2DShapesAndTextBitmapActivity])
  }
}
