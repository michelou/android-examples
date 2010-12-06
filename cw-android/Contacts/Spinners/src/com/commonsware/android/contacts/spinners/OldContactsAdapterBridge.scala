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

import scala.android.provider.Contacts

class OldContactsAdapterBridge extends ContactsAdapterBridge {

  def buildNameAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(
      Contacts.People._ID,
      Contacts.PeopleColumns.NAME)
    val cursor =
      a.managedQuery(Contacts.People.CONTENT_URI,
                     PROJECTION, null, null,
                     Contacts.People.DEFAULT_SORT_ORDER)

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_1,
                            cursor,
                            Array(Contacts.PeopleColumns.NAME),
                            Array(android.R.id.text1))
  }
  
  def buildPhonesAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(
      Contacts.Phones._ID,
      Contacts.Phones.NAME,
      Contacts.Phones.NUMBER)
    val cursor =
      a.managedQuery(Contacts.Phones.CONTENT_URI,
                     PROJECTION, null, null,
                     Contacts.Phones.DEFAULT_SORT_ORDER)

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_2,
                            cursor,
                            Array(Contacts.Phones.NAME, Contacts.Phones.NUMBER),
                            Array(android.R.id.text1, android.R.id.text2))
  }

  def buildEmailAdapter(a: Activity): ListAdapter = {
    val PROJECTION = Array(
      Contacts.ContactMethods._ID,
      Contacts.ContactMethods.DATA,
      Contacts.PeopleColumns.NAME)
    val cursor =
      a.managedQuery(Contacts.ContactMethods.CONTENT_EMAIL_URI,
                            PROJECTION, null, null,
                            Contacts.ContactMethods.DEFAULT_SORT_ORDER);

    new SimpleCursorAdapter(a,
                            android.R.layout.simple_list_item_2,
                            cursor,
                            Array(Contacts.PeopleColumns.NAME,
                                  Contacts.ContactMethods.DATA),
                            Array(android.R.id.text1, android.R.id.text2))
  }
}
