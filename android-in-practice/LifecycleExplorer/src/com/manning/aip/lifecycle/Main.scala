package com.manning.aip.lifecycle

import android.content.Intent
import android.os.{Bundle, SystemClock}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, Chronometer}

class Main extends LifecycleActivity {   

  private var finishBtn: Button = _
  private var activity2: Button = _
  private var chrono: Chronometer = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    finishBtn = findViewById(R.id.finishButton).asInstanceOf[Button]
    finishBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        finish()
      }
    }
    activity2 = findViewById(R.id.activity2Button).asInstanceOf[Button]
    activity2 setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[Activity2]))
      }
    }
    chrono = findViewById(R.id.chronometer).asInstanceOf[Chronometer]
  }

  override protected def onResume() {
    super.onResume()
    chrono setBase SystemClock.elapsedRealtime
    chrono.start()
  }

  override protected def onPause() {
    chrono.stop()
    super.onPause()
  }
}
