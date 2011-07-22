package com.manning.aip.helloandroid

import android.app.Activity
import android.os.Bundle

class Main extends Activity {
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
  }
}
