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

import android.app.{Activity, Dialog, ListActivity}
import android.content.{ContentUris, DialogInterface, Intent}
import android.content.res.Resources
import android.database.Cursor
import android.graphics.{Bitmap, Canvas, Paint, Rect, Typeface}
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.net.Uri
import android.os.Bundle
import android.provider.Contacts
import android.provider.Contacts.{People, PeopleColumns, Phones, PhonesColumns}
import android.view.View
import android.widget.{ArrayAdapter, ListView}

/**
 * Presents the user with a list of types of shortucts that can be created.
 * When Any Cut is launched through the home screen this is the activity that comes up.
 */
class CreateShortcutActivity extends ListActivity with DialogInterface.OnClickListener
                                                  with DialogInterface.OnCancelListener {
  import CreateShortcutActivity._  // companion object

  private var mEditorIntent: Intent = _

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)

    setListAdapter(ArrayAdapter.createFromResource(this, R.array.mainMenu,
                android.R.layout.simple_list_item_1))
  }

  override protected def onListItemClick(list: ListView, view: View, position: Int, id: Long) {
    position match {
      case LIST_ITEM_DIRECT_CALL =>
        val intent = new Intent(Intent.ACTION_PICK, Phones.CONTENT_URI)
        intent.putExtra(Contacts.Intents.UI.TITLE_EXTRA_KEY,
                        getText(R.string.callShortcutActivityTitle))
        startActivityForResult(intent, REQUEST_PHONE)

      case LIST_ITEM_DIRECT_TEXT =>
        val intent = new Intent(Intent.ACTION_PICK, Phones.CONTENT_URI)
        intent.putExtra(Contacts.Intents.UI.TITLE_EXTRA_KEY,
                        getText(R.string.textShortcutActivityTitle))
        startActivityForResult(intent, REQUEST_TEXT)

      case LIST_ITEM_ACTIVITY =>
        val intent = new Intent();
        intent.setClass(this, classOf[ActivityPickerActivity])
        startActivityForResult(intent, REQUEST_ACTIVITY)

      case LIST_ITEM_CUSTOM =>
        val intent = new Intent()
        intent.setClass(this, classOf[CustomShortcutCreatorActivity])
        startActivityForResult(intent, REQUEST_CUSTOM)

      case _ =>
    }
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, result: Intent) {
    if (resultCode != Activity.RESULT_OK) return

    requestCode match {
      case REQUEST_PHONE =>
         startShortcutEditor(generatePhoneShortcut(result, R.drawable.sym_action_call,
                        "tel", Intent.ACTION_CALL))

      case REQUEST_TEXT =>
         startShortcutEditor(generatePhoneShortcut(result, R.drawable.sym_action_sms,
                        "smsto", Intent.ACTION_SENDTO))

      case REQUEST_ACTIVITY | REQUEST_CUSTOM =>
         startShortcutEditor(result)

      case _ =>
    }
  }

  override protected def onCreateDialog(dialogId: Int): Dialog =
    dialogId match {
      case DIALOG_SHORTCUT_EDITOR =>
        new ShortcutEditorDialog(this, this, this)
      case _ =>
        super.onCreateDialog(dialogId)
    }

  override protected def onPrepareDialog(dialogId: Int, dialog: Dialog) {
    dialogId match {
      case DIALOG_SHORTCUT_EDITOR if mEditorIntent != null =>
        // If the editor intent hasn't been set already set it
        val editor = dialog.asInstanceOf[ShortcutEditorDialog]
        editor setIntent mEditorIntent
        mEditorIntent = null
      case _ =>
    }
  }

  /*
   * Starts the shortcut editor
   *
   * @param shortcutIntent The shortcut intent to edit
   */
  private def startShortcutEditor(shortcutIntent: Intent) {
    mEditorIntent = shortcutIntent
    showDialog(DIALOG_SHORTCUT_EDITOR)
  }

  def onCancel(dialog: DialogInterface) {
    // Remove the dialog, it won't be used again
    removeDialog(DIALOG_SHORTCUT_EDITOR)
  }

  def onClick(dialog: DialogInterface, which: Int) {
    if (which == DialogInterface.BUTTON1) {
      // OK button
      val editor = dialog.asInstanceOf[ShortcutEditorDialog]
      val shortcut = editor.getIntent
      setResult(Activity.RESULT_OK, shortcut)
      finish()
    }

    // Remove the dialog, it won't be used again
    removeDialog(DIALOG_SHORTCUT_EDITOR)
  }

  /**
   * Returns an Intent describing a direct text message shortcut.
   *
   * @param result The result from the phone number picker
   * @return an Intent describing a phone number shortcut
   */
  private def generatePhoneShortcut(result: Intent, actionResId: Int, scheme: String, action: String): Intent = {
    var phoneUri = result.getData
    var personId = 0L
    var name: String = null
    var number: String = null
    var typ = 0
    val cursor = getContentResolver.query(phoneUri,
                 Array(Phones.PERSON_ID, PeopleColumns.DISPLAY_NAME, PhonesColumns.NUMBER, PhonesColumns.TYPE),
                null, null, null)
    try {
      cursor.moveToFirst()
      personId = cursor getLong 0
      name = cursor getString 1
      number = cursor getString 2
      typ = cursor getInt 3
    } finally {
      if (cursor != null) cursor.close()
    }
    val intent = new Intent()
    val personUri = ContentUris.withAppendedId(People.CONTENT_URI, personId)
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON,
                generatePhoneNumberIcon(personUri, typ, actionResId))

    // Make the URI a direct tel: URI so that it will always continue to work
    phoneUri = Uri.fromParts(scheme, number, null)
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(action, phoneUri))
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name)
    intent
  }

  /**
   * Generates a phone number shortcut icon. Adds an overlay describing the type of the phone
   * number, and if there is a photo also adds the call action icon.
   *
   * @param personUri The person the phone number belongs to
   * @param type The type of the phone number
   * @param actionResId The ID for the action resource
   * @return The bitmap for the icon
   */
  private def generatePhoneNumberIcon(personUri: Uri, typ: Int, actionResId: Int): Bitmap = {
    val r = getResources
    var drawPhoneOverlay = true

    var photo = People.loadContactPhoto(this, personUri, 0, null)
    if (photo == null) {
      // If there isn't a photo use the generic phone action icon instead
      val phoneIcon = getPhoneActionIcon(r, actionResId)
      if (phoneIcon != null) {
        photo = phoneIcon
        drawPhoneOverlay = false
      } else {
        return null
      }
    }

    // Setup the drawing classes
    val iconSize = r.getDimension(android.R.dimen.app_icon_size).toInt
    val icon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
    val canvas = new Canvas(icon)

    // Copy in the photo
    val photoPaint = new Paint()
    photoPaint setDither true
    photoPaint setFilterBitmap true
    val src = new Rect(0,0, photo.getWidth, photo.getHeight)
    val dst = new Rect(0,0, iconSize,iconSize)
    canvas.drawBitmap(photo, src, dst, photoPaint)

    // Create an overlay for the phone number type
    val overlay = typ match {
      case PhonesColumns.TYPE_HOME   => "H"
      case PhonesColumns.TYPE_MOBILE => "M"
      case PhonesColumns.TYPE_WORK   => "W"
      case PhonesColumns.TYPE_PAGER  => "P"
      case PhonesColumns.TYPE_OTHER  => "O"
      case _ => null
    }
    if (overlay != null) {
      val textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG)
      textPaint setTextSize 20.0f
      textPaint setTypeface Typeface.DEFAULT_BOLD
      textPaint setColor r.getColor(R.color.textColorIconOverlay)
      textPaint.setShadowLayer(3f, 1, 1, r.getColor(R.color.textColorIconOverlayShadow))
      canvas.drawText(overlay, 2, 16, textPaint)
    }

    // Draw the phone action icon as an overlay
    if (ENABLE_ACTION_ICON_OVERLAYS && drawPhoneOverlay) {
      val phoneIcon = getPhoneActionIcon(r, actionResId)
      if (phoneIcon != null) {
        src.set(0,0, phoneIcon.getWidth, phoneIcon.getHeight)
        val iconWidth = icon.getWidth
        dst.set(iconWidth - 20, -1, iconWidth, 19)
        canvas.drawBitmap(phoneIcon, src, dst, photoPaint)
      }
    }

    icon
  }

  /**
   * Returns the icon for the phone call action.
   *
   * @param r The resources to load the icon from
   * @param resId The resource ID to load
   * @return the icon for the phone call action
   */
  private def getPhoneActionIcon(r: Resources, resId: Int): Bitmap = {
    val phoneIcon = r getDrawable resId
    phoneIcon match {
      case bd: BitmapDrawable => bd.getBitmap
      case _ => null
    }
  }
}

object CreateShortcutActivity {
  private final val ENABLE_ACTION_ICON_OVERLAYS = false

  private final val REQUEST_PHONE    = 1
  private final val REQUEST_TEXT     = 2
  private final val REQUEST_ACTIVITY = 3
  private final val REQUEST_CUSTOM   = 4

  private final val LIST_ITEM_DIRECT_CALL = 0
  private final val LIST_ITEM_DIRECT_TEXT = 1
  private final val LIST_ITEM_ACTIVITY    = 2
  private final val LIST_ITEM_CUSTOM      = 3

  private final val DIALOG_SHORTCUT_EDITOR = 1
}
