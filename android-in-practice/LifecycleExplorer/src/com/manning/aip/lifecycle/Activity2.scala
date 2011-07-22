package com.manning.aip.lifecycle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

class Activity2 extends LifecycleActivity {

  private var activity3: Button = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity2)

    activity3 = findViewById(R.id.activity3Button).asInstanceOf[Button]
    activity3 setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Activity2.this, classOf[Activity3]))
      }
    }
  }
}