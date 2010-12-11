package com.example.android.helloviews.tests

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert._

import com.example.android.helloviews
import com.example.android.helloviews.HelloViews

class HelloViewsTest extends ActivityInstrumentationTestCase2[HelloViews]("com.example.helloviews", classOf[HelloViews]) {

  private var mActivity: HelloViews = _ // the activity under test
  private var mView: TextView = _       // the activity's TextView (the only view)
  private var resourceString: String = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()
    mActivity = this.getActivity
    mView = mActivity.findViewById(helloviews.R.id.textview).asInstanceOf[TextView]
    resourceString = mActivity getString helloviews.R.string.app_name
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
