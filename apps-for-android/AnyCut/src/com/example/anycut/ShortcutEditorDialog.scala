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

import android.app.AlertDialog
import android.content.{Context, DialogInterface, Intent}
import android.content.DialogInterface.{OnCancelListener, OnClickListener}
import android.content.Intent.ShortcutIconResource
import android.graphics.Bitmap
import android.os.Bundle
import android.text.{Editable, TextUtils, TextWatcher}
import android.view.View
import android.widget.{EditText, ImageView}

/**
 * A dialog that can edit a shortcut intent. For now the icon is displayed, and only
 * the name may be edited.
 */
class ShortcutEditorDialog(context: Context, onClick: OnClickListener,
                           onCancel: OnCancelListener) extends AlertDialog(context)
                                                          with OnClickListener
                                                          with TextWatcher {
  import ShortcutEditorDialog._  // companion object

  private var mCreated = false

  private var mIntent: Intent = _
  private var mIconView: ImageView = _
  private var mNameView: EditText = _
  private val mOnClick = onClick
  private val mOnCancel = onCancel

  // Setup the dialog
  {
    val view = getLayoutInflater.inflate(R.layout.shortcut_editor, null, false)
    setTitle(R.string.shortcutEditorTitle)
    setButton(context getText android.R.string.ok, this)
    setButton2(context getText android.R.string.cancel, mOnClick)
    setOnCancelListener(mOnCancel)
    setCancelable(true)
    setView(view)

    mIconView = view.findViewById(R.id.shortcutIcon).asInstanceOf[ImageView]
    mNameView = view.findViewById(R.id.shortcutName).asInstanceOf[EditText]
  }

  def onClick(dialog: DialogInterface, which: Int) {
    if (which == DialogInterface.BUTTON1) {
      val name = mNameView.getText.toString
      if (TextUtils.isEmpty(name)) {
        // Don't allow an empty name
        mNameView setError getContext.getText(R.string.errorEmptyName)
        return
      }
      mIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
    }
    mOnClick.onClick(dialog, which)
  }

  override protected def onCreate(state: Bundle) {
    super.onCreate(state)
    if (state != null) {
      mIntent = state getParcelable STATE_INTENT
    }

    mCreated = true

    // If an intent is set make sure to load it now that it's safe
    if (mIntent != null) {
      loadIntent(mIntent)
    }
  }

  override def onSaveInstanceState(): Bundle = {
    val state = super.onSaveInstanceState()
    state.putParcelable(STATE_INTENT, getIntent)
    state
  }

  def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    // Do nothing
  }

  def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    // Do nothing
  }

  def afterTextChanged(text: Editable) {
    val msg = if (text.length == 0) getContext getText R.string.errorEmptyName else null
    mNameView setError msg
  }

  /**
   * Saves the current state of the editor into the intent and returns it.
   *
   * @return the intent for the shortcut being edited
   */
  def getIntent: Intent = {
    mIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, mNameView.getText.toString)
    mIntent
  }

  /**
   * Reads the state of the shortcut from the intent and sets up the editor
   *
   * @param intent A shortcut intent to edit
   */
  def setIntent(intent: Intent) {
    mIntent = intent
    if (mCreated) {
      loadIntent(intent)
    }
  }

  /**
   * Loads the editor state from a shortcut intent.
   *
   * @param intent The shortcut intent to load the editor from
   */
  private def loadIntent(intent: Intent) {
    // Show the icon
    val iconBitmap = intent getParcelableExtra Intent.EXTRA_SHORTCUT_ICON
    if (iconBitmap != null) {
      mIconView setImageBitmap iconBitmap
    } else {
      val iconRes: ShortcutIconResource =
        intent getParcelableExtra Intent.EXTRA_SHORTCUT_ICON_RESOURCE
      if (iconRes != null) {
        val res = getContext.getResources.getIdentifier(iconRes.resourceName, null,
                        iconRes.packageName)
        mIconView setImageResource res
      } else {
        mIconView setVisibility View.INVISIBLE
      }
    }

    // Fill in the name field for editing
    mNameView addTextChangedListener this
    mNameView setText (intent getStringExtra Intent.EXTRA_SHORTCUT_NAME)

    // Ensure the intent has the proper flags
    intent addFlags Intent.FLAG_ACTIVITY_CLEAR_TOP
  }
}

object ShortcutEditorDialog {
  final val STATE_INTENT = "intent"
}
