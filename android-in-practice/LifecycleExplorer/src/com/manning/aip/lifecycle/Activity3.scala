package com.manning.aip.lifecycle

import android.os.Bundle
import android.widget.{TextView, Toast}

import java.util.Date

class Activity3 extends LifecycleActivity {
  import Activity3._  // companion object

  private var numResumes: TextView = _
  private var count: Int = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity3)
    numResumes = findViewById(R.id.numResumes).asInstanceOf[TextView]
      
    // if the last non configuration object is present, show it
    // (can use this in onCreate and onStart)
    val date = this.getLastNonConfigurationInstance.asInstanceOf[Date]
    if (date != null) {
      Toast.makeText(this, "\"LastNonConfiguration\" object present: " + date, Toast.LENGTH_LONG).show()
    }   
  }

  override protected def onResume() {
    super.onResume()

    numResumes setText count.toString
    count += 1
  }

  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    if ((savedInstanceState != null) && savedInstanceState.containsKey(COUNT_KEY)) {
      count = savedInstanceState getInt COUNT_KEY
    }
    super.onRestoreInstanceState(savedInstanceState)
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    outState.putInt(COUNT_KEY, count)
    super.onSaveInstanceState(outState)
  }
   
  override def onRetainNonConfigurationInstance(): AnyRef = {
    new Date()
  }  
}

object Activity3 {
  final val COUNT_KEY = "cKey"
}
