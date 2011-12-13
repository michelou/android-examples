/*
 * Copyright (C) 2008 Google Inc.
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

package com.example.anycut

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast

/**
 * The activity that shows up in the list of all applications. It has a button
 * allowing the user to create a new shortcut, and guides them to using Any Cut
 * through long pressing on the location of the desired shortcut.
 */
class FrontDoorActivity extends Activity with OnClickListener {
  import FrontDoorActivity._  // companion object

  override protected def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    setContentView(R.layout.front_door)

    // Setup the new shortcut button
    val view = findViewById(R.id.newShortcut)
    if (view != null) {
      view setOnClickListener this
    }
  }

  def onClick(view: View) {
    view.getId match {
      case R.id.newShortcut =>
        // Start the activity to create a shortcut intent
        val intent = new Intent(this, classOf[CreateShortcutActivity])
        startActivityForResult(intent, REQUEST_SHORTCUT)
      case _ =>
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, result: Intent) {
    if (resultCode != Activity.RESULT_OK) return

    requestCode match {
      case REQUEST_SHORTCUT =>
        // Boradcast an intent that tells the home screen to create a new shortcut
        result setAction "com.android.launcher.action.INSTALL_SHORTCUT"
        sendBroadcast(result)

        // Inform the user that the shortcut has been created
        Toast.makeText(this, R.string.shortcutCreated, Toast.LENGTH_SHORT).show()
      case _  =>
    }
  }
}

object FrontDoorActivity {
  private final val REQUEST_SHORTCUT = 1
}
