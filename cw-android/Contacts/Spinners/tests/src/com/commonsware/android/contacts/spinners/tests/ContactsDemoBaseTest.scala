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

import android.test.AndroidTestCase
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ListView, Spinner}
import junit.framework.Assert._

import com.commonsware.android.contacts.spinners

class ContactsDemoBaseTest extends AndroidTestCase {
  private var list: ListView = _
  private var spinner: Spinner = _
  private var root: ViewGroup = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()

    val inflater = LayoutInflater.from(getContext)

    root = inflater.inflate(spinners.R.layout.main, null).asInstanceOf[ViewGroup]
    root.measure(480, 320)
    root.layout(0, 0, 480, 320)

    list = root.findViewById(android.R.id.list).asInstanceOf[ListView]
    spinner = root.findViewById(spinners.R.id.spinner).asInstanceOf[Spinner]
  }

  def testExists() {
    assertNotNull(list)
    assertNotNull(spinner)
  }

  def testRelativePosition() {
    assertTrue(list.getTop >= spinner.getBottom)
    assertTrue(list.getLeft == spinner.getLeft)
    assertTrue(list.getRight == spinner.getRight)
  }
}
