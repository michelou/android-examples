/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.snake.tests

import android.test.ActivityInstrumentationTestCase2
import android.widget.TextView
import junit.framework.Assert._

import com.example.android.snake
import com.example.android.snake.Snake

/**
 * Make sure that the main launcher activity opens up properly, which will be
 * verified by {@link ActivityTestCase#testActivityTestCaseSetUpProperly}.
 */
class SnakeTest extends ActivityInstrumentationTestCase2[Snake]("com.example.android.snake", classOf[Snake]) {

  private var mActivity: Snake = _ // the activity under test
  private var mView: TextView = _  // the activity's TextView (the only view)
  private var resourceString: String = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()
    mActivity = this.getActivity
    mView = mActivity.findViewById(snake.R.id.text).asInstanceOf[TextView]
    resourceString = mActivity getString snake.R.string.app_name
  }

  def testPreconditions() {
    assertNotNull(mView)
  }

  def testText() {
    val expected = resourceString startsWith "Snake"
    val actual = mView.getText.toString startsWith "Snake"
    assertEquals(expected, actual)
  }

}
