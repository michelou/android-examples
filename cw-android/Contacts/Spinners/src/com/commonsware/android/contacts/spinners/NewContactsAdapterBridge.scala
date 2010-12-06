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

import android.app.Activity
import android.database.Cursor
import android.widget.{ListAdapter, SimpleCursorAdapter}

import scala.android.provider.ContactsContract.Contacts
import scala.android.provider.ContactsContract.CommonDataKinds.{Email, Phone}

class NewContactsAdapterBridge extends ContactsAdapterBridge {
  def buildNameAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(Contacts._ID, Contacts.DISPLAY_NAME)
    val cursor =
      a.managedQuery(Contacts.CONTENT_URI, PROJECTION, null, null, null)

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_1,
                            cursor,
                            Array(Contacts.DISPLAY_NAME),
                            Array(android.R.id.text1))
  }
  
  def buildPhonesAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(Contacts._ID, Contacts.DISPLAY_NAME, Phone.NUMBER)
    val cursor =
      a.managedQuery(Phone.CONTENT_URI, PROJECTION, null, null, null)

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_2,
                            cursor,
                            Array(Contacts.DISPLAY_NAME, Phone.NUMBER),
                            Array(android.R.id.text1, android.R.id.text2))
  }

  def buildEmailAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(Contacts._ID, Contacts.DISPLAY_NAME, Email.DATA)
    val cursor =
      a.managedQuery(Email.CONTENT_URI, PROJECTION, null, null, null)

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_2,
                            cursor,
                            Array(Contacts.DISPLAY_NAME, Email.DATA),
                            Array(android.R.id.text1, android.R.id.text2))
  }
}
