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

package com.commonsware.android.contacts.spinners

import android.app.ListActivity
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ArrayAdapter, ListAdapter, Spinner}

class ContactSpinners extends ListActivity
                         with AdapterView.OnItemSelectedListener {
  import ContactSpinners._  // companion object

  private val listAdapters = new Array[ListAdapter](3)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    initListAdapters()

    val spin = findViewById(R.id.spinner).asInstanceOf[Spinner]
    spin setOnItemSelectedListener this

    val aa = new ArrayAdapter[String](this,
                              android.R.layout.simple_spinner_item,
                              options)

    aa setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    spin setAdapter aa
  }

  def onItemSelected(parent: AdapterView[_], v: View, position: Int, id: Long) {
    setListAdapter(listAdapters(position))
  }

  def onNothingSelected(parent: AdapterView[_]) {
    // ignore
  }

  private def initListAdapters() {
    listAdapters(0) = ContactsAdapterBridge.INSTANCE buildNameAdapter this
    listAdapters(1) = ContactsAdapterBridge.INSTANCE buildPhonesAdapter this
    listAdapters(2) = ContactsAdapterBridge.INSTANCE buildEmailAdapter this
  }

}

object ContactSpinners {

  private val options = Array(
    "Contact Names",
    "Contact Names & Numbers",
    "Contact Names & Email Addresses")

}

