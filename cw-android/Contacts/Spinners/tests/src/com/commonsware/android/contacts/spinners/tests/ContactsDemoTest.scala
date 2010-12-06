/***
    Copyright (c) 2008-2010 CommonsWare, LLC
    
    Licensed under the Apache License, Version 2.0 (the "License"); you may
    not use this file except in compliance with the License. You may obtain
    a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.commonsware.android.contacts.spinners.tests

import android.test.{ActivityInstrumentationTestCase2 => ActivityTestCase}
import android.widget.{ListView, Spinner}
import junit.framework.Assert._

import com.commonsware.android.contacts.spinners
import com.commonsware.android.contacts.spinners.ContactSpinners

class ContactsDemoTest extends ActivityTestCase[ContactSpinners]("com.commonsware.android.contacts.spinners", classOf[ContactSpinners]) {
  private var list: ListView = _
  private var spinner: Spinner = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()

    val activity = getActivity

    list = activity.findViewById(android.R.id.list).asInstanceOf[ListView]
    spinner = activity.findViewById(spinners.R.id.spinner).asInstanceOf[Spinner]
  }

  def testSpinnerCount() {
    assertTrue(spinner.getAdapter.getCount == 3)
  }

  def testListDefaultCount() {
    assertTrue(list.getAdapter.getCount > 0)
  }
}
