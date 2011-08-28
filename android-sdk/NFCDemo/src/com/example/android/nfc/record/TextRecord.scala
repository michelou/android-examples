/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.example.android.nfc.record

import android.app.Activity
import android.nfc.NdefRecord
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView

import com.google.common.base.{Charsets, Preconditions}

import com.example.android.nfc.R

/**
 * An NFC Text Record
 */
class TextRecord(languageCode: String, text: String) extends ParsedNdefRecord {

  /** ISO/IANA language code */
  private final val mLanguageCode = Preconditions.checkNotNull(languageCode)

  private final val mText = Preconditions.checkNotNull(text)

  def getView(activity: Activity, inflater: LayoutInflater,
              parent: ViewGroup, offset: Int): View = {
    val text = inflater.inflate(R.layout.tag_text, parent, false).asInstanceOf[TextView]
    text setText mText
    text
  }

  def getText: String = mText

  /**
   * Returns the ISO/IANA language code associated with this text element.
   */
  def getLanguageCode: String = mLanguageCode

}
  
object TextRecord {

  // TODO: deal with text fields which span multiple NdefRecords
  def parse(record: NdefRecord): TextRecord = {
    Preconditions.checkArgument(record.getTnf == NdefRecord.TNF_WELL_KNOWN)
    Preconditions.checkArgument(record.getType == NdefRecord.RTD_TEXT)
    try {
      val payload = record.getPayload
      /*
       * payload[0] contains the "Status Byte Encodings" field, per the
       * NFC Forum "Text Record Type Definition" section 3.2.1.
       *
       * bit7 is the Text Encoding Field.
       *
       * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
       * The text is encoded in UTF16
       *
       * Bit_6 is reserved for future use and must be set to zero.
       *
       * Bits 5 to 0 are the length of the IANA language code.
       */
      val textEncoding = if ((payload(0) & 0200) == 0) "UTF-8" else "UTF-16"
      val languageCodeLength = payload(0) & 0077
      val languageCode = new String(payload, 1, languageCodeLength, Charsets.US_ASCII)
      val text = new String(payload, languageCodeLength + 1,
                            payload.length - languageCodeLength - 1, textEncoding)
      new TextRecord(languageCode, text)
    } catch {
      case e: java.io.UnsupportedEncodingException =>
        // should never happen unless we get a malformed tag.
        throw new IllegalArgumentException(e)
    }
  }

  def isText(record: NdefRecord): Boolean =
    try { parse(record); true }
    catch { case e: IllegalArgumentException => false }
}
