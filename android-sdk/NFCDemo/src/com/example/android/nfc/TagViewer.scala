/*
 * Copyright (C) 2010 The Android Open Source Project
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
package com.example.android.nfc

import android.app.Activity
import android.content.Intent
import android.nfc.{NdefMessage, NdefRecord, NfcAdapter}
import android.os.{Bundle, Parcelable}
import android.util.Log
import android.view.{LayoutInflater, WindowManager}
import android.widget.{LinearLayout, TextView}

import com.example.android.nfc.record.ParsedNdefRecord

/**
 * An {@link Activity} which handles a broadcast of a new tag that the device
 * just discovered.
 */
class TagViewer extends Activity {
  import TagViewer._  // companion object

  var mTitle: TextView = _
  var mTagContent: LinearLayout = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tag_viewer)
    mTagContent = findViewById(R.id.list).asInstanceOf[LinearLayout]
    mTitle = findViewById(R.id.title).asInstanceOf[TextView]
    resolveIntent(getIntent)
  }

  private def resolveIntent(intent: Intent) {
    // Parse the intent
    val action = intent.getAction
    if (NfcAdapter.ACTION_TAG_DISCOVERED equals action) {
      // When a tag is discovered we send it to the service to be save. We
      // include a PendingIntent for the service to call back onto. This
      // will cause this activity to be restarted with onNewIntent(). At
      // that time we read it from the database and view it.
      val rawMsgs = intent getParcelableArrayExtra NfcAdapter.EXTRA_NDEF_MESSAGES
      val msgs = if (rawMsgs != null) {
        val ms = new Array[NdefMessage](rawMsgs.length)
        for (i <- 0 until rawMsgs.length)
          ms(i) = rawMsgs(i).asInstanceOf[NdefMessage]
        ms
      } else {
        // Unknown tag type
        val empty = new Array[Byte](0)
        val record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = new NdefMessage(Array(record))
        Array(msg)
      }
      // Setup the views
      setTitle(R.string.title_scanned_tag)
      buildTagViews(msgs)
    } else {
      Log.e(TAG, "Unknown intent " + intent)
      finish()
    }
  }

  def buildTagViews(msgs: Array[NdefMessage]) {
    if (msgs == null || msgs.length == 0)
      return

    val inflater = LayoutInflater.from(this)
    val content = mTagContent
    // Clear out any old views in the content area, for example if you scan
    // two tags in a row.
    content.removeAllViews()
    // Parse the first message in the list
    // Build views for all of the sub records
    val records = NdefMessageParser.parse(msgs(0))
    val size = records.size
    for (i <- 0 until size) {
      val record = records(i)
      content addView record.getView(this, inflater, content, i)
      inflater.inflate(R.layout.tag_divider, content, true)
    }
  }

  override def onNewIntent(intent: Intent) {
    setIntent(intent)
    resolveIntent(intent)
  }

  override def setTitle(title: CharSequence) {
    mTitle setText title
  }
}

object TagViewer {
  final val TAG = "TagViewer"

  /**
   * This activity will finish itself in this amount of time if the user
   * doesn't do anything.
   */
  final val ACTIVITY_TIMEOUT_MS = 1 * 1000
}
 