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
 * limitations under the License
 */
package com.example.android.nfc.simulator

import android.app.ListActivity
import android.content.Intent
import android.nfc.{NdefMessage, NdefRecord, NfcAdapter}
import android.os.Bundle
import android.view.View
import android.widget.{ArrayAdapter, ListView}

import com.google.common.base.{Charsets, Preconditions}

import java.util.Locale

/**
 * A activity that launches tags as if they had been scanned.
 */
class FakeTagsActivity extends ListActivity {
  import FakeTagsActivity._  // companion object

  private var mAdapter: ArrayAdapter[TagDescription] = _

  override def onCreate(savedState: Bundle) {
    super.onCreate(savedState)
    val adapter = new ArrayAdapter[TagDescription](
      this, android.R.layout.simple_list_item_1, android.R.id.text1)
    adapter add new TagDescription(
      "Broadcast NFC Text Tag", MockNdefMessages.ENGLISH_PLAIN_TEXT)
    adapter add new TagDescription(
      "Broadcast NFC SmartPoster URL & text", MockNdefMessages.SMART_POSTER_URL_AND_TEXT)
    adapter add new TagDescription(
      "Broadcast NFC SmartPoster URL", MockNdefMessages.SMART_POSTER_URL_NO_TEXT)
    setListAdapter(adapter)
    mAdapter = adapter
  }

  override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val description = mAdapter getItem position
    val intent = new Intent(NfcAdapter.ACTION_TAG_DISCOVERED)
    intent.putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, description.msgs)
    startActivity(intent)
  }
}

object FakeTagsActivity {
  final val TAG = "FakeTagsActivity"

  final val UID = Array[Byte](0x05, 0x00, 0x03, 0x08)
  private final val EMPTY = Array[Byte]()

  def newTextRecord(text: String, locale: Locale, encodeInUtf8: Boolean): NdefRecord = {
    Preconditions.checkNotNull(text)
    Preconditions.checkNotNull(locale)
    val langBytes = locale.getLanguage getBytes Charsets.US_ASCII
    val utfEncoding = if (encodeInUtf8) Charsets.UTF_8 else Charsets.UTF_16
    val textBytes = text getBytes utfEncoding
    val utfBit = if (encodeInUtf8) 0 else (1 << 7)
    val status = (utfBit + langBytes.length).toChar
    val data = Array[Byte](status.toByte) ++ langBytes ++ textBytes
    new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, EMPTY, data)
  }

  def newMimeRecord(typ: String, data: Array[Byte]): NdefRecord = {
    Preconditions.checkNotNull(typ)
    Preconditions.checkNotNull(data)
    val typeBytes = typ getBytes Charsets.US_ASCII
    new NdefRecord(NdefRecord.TNF_MIME_MEDIA, typeBytes, EMPTY, data)
  }

  class TagDescription(val title: String, bytes: Array[Byte]) {
   val msgs =
     try Array(new NdefMessage(bytes))
     catch {
       case e: Exception =>
         throw new RuntimeException("Failed to create tag description", e)
     }

    override def toString = title
  }

}
