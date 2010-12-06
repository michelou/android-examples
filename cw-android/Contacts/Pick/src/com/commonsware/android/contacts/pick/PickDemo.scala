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

package com.commonsware.android.contacts.pick

import android.content.Intent
import android.net.Uri
import android.os.{Build, Bundle}
import android.provider.Contacts
import android.util.Log
import android.view.View
import android.widget.{Button, Toast}

import scala.android.app.Activity

class PickDemo extends Activity {
  import PickDemo._  // companion object

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    
    if (CONTENT_URI == null) {
      Toast
        .makeText(this, "We are experiencing technical difficulties...",
                  Toast.LENGTH_LONG)
        .show()
      finish()
      return
    }
    
    setContentView(R.layout.main)
    
    val btn = findButton(R.id.pick)

    btn setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        val i = new Intent(Intent.ACTION_PICK, CONTENT_URI)

        startActivityForResult(i, PICK_REQUEST)
      }
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int,
                                          data: Intent) {
    if (requestCode == PICK_REQUEST) {
      if (resultCode == Activity.RESULT_OK) {
          startActivity(new Intent(Intent.ACTION_VIEW, data.getData))
      }
    }
  }
}

object PickDemo {

  private final val PICK_REQUEST = 1337
  private final val CONTENT_URI: Uri = {
    val sdk = Build.VERSION.SDK.toInt
    
    if (sdk >= 5) {
      try {
        val clazz = Class.forName("android.provider.ContactsContract$Contacts")
      
        (clazz.getField("CONTENT_URI") get clazz).asInstanceOf[Uri]
      }
      catch {
        case t: Throwable =>
        Log.e("PickDemo", "Exception when determining CONTENT_URI", t)
        null
      }
    }
    else {
      Contacts.People.CONTENT_URI
    }
  }

}
