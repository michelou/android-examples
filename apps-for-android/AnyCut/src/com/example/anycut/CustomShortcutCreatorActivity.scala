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
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText

/**
 * A simple activity to allow the user to manually type in an Intent.
 */
class CustomShortcutCreatorActivity extends Activity with View.OnClickListener {

  override protected def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    setContentView(R.layout.custom_shortcut_creator)

    findViewById(R.id.ok) setOnClickListener this
    findViewById(R.id.cancel) setOnClickListener this
  }

  def onClick(view: View) {
    view.getId match {
      case R.id.ok =>
        val intent = createShortcutIntent()
        setResult(Activity.RESULT_OK, intent)
        finish()

      case R.id.cancel =>
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
  }

  private def createShortcutIntent(): Intent = {
    val intent = new Intent()

    var view = findViewById(R.id.action).asInstanceOf[EditText]
    intent setAction view.getText.toString

    view = findViewById(R.id.data).asInstanceOf[EditText]
    val data = view.getText.toString
    view = findViewById(R.id.`type`).asInstanceOf[EditText]
    val typ = view.getText.toString

    val dataEmpty = TextUtils.isEmpty(data)
    val typeEmpty = TextUtils.isEmpty(typ)
    if (!dataEmpty && typeEmpty)
      intent setData Uri.parse(data)
    else if (!typeEmpty && dataEmpty)
      intent setType typ
    else if (!typeEmpty && !dataEmpty)
      intent.setDataAndType(Uri.parse(data), typ)

    new Intent().putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)
  }
}
