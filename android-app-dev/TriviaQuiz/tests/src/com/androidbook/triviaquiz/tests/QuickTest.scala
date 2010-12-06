package com.androidbook.triviaquiz.tests

import android.test.AndroidTestCase
import android.util.Log

import junit.framework.Assert._

class QuickTest extends AndroidTestCase {

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()
  }

  @throws(classOf[Exception])
  override protected def tearDown() {
    super.tearDown()
  }

  def testSimple() {
    Log.d("tst", "Simple Test")
    assertTrue("should be true", 1 == 1)
  }

}
