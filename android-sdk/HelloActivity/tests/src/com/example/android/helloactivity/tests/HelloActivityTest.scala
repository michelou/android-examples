package com.example.android.helloactivity.tests

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert._

import com.example.android.helloactivity
import com.example.android.helloactivity.HelloActivity

class HelloActivityTest extends ActivityInstrumentationTestCase2[HelloActivity]("com.example.helloactivity", classOf[HelloActivity]) {

  private var mActivity: HelloActivity = _ // the activity under test
  private var mView: TextView = _          // the activity's TextView (the only view)
  private var resourceString: String = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()
    mActivity = this.getActivity
    mView = mActivity.findViewById(helloactivity.R.id.textview).asInstanceOf[TextView]
    resourceString = mActivity getString helloactivity.R.string.app_name
  }

  def testPreconditions() {
    assertNotNull(mView)
  }

  def testText() {
    val expected = resourceString startsWith "Hello"
    val actual = mView.getText.toString startsWith "Hello"
    assertEquals(expected, actual)
  }

}
