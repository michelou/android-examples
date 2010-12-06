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

package com.commonsware.android.inserter

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.Intents.Insert
import android.view.View
import android.widget.{Button, EditText}

import scala.android.app.Activity

class ContactsInserter extends Activity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    val btn = findButton(R.id.insert)

    btn setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        var fld = findEditText(R.id.name)
        val name = fld.getText.toString

        fld = findEditText(R.id.phone)
        val phone = fld.getText.toString

        fld = findEditText(R.id.email)
        val email = fld.getText.toString

        val i = new Intent(Intent.ACTION_INSERT_OR_EDIT)
        i.setType(Contacts.CONTENT_ITEM_TYPE)
        i.putExtra(Insert.NAME, name)
        i.putExtra(Insert.PHONE, phone)
        i.putExtra(Insert.EMAIL, email)
        startActivity(i)
      }
    }
  }
}
