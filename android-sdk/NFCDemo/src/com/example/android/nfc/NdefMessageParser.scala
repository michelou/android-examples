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
package com.example.android.nfc

import android.nfc.{NdefMessage, NdefRecord}

import scala.collection.mutable.ListBuffer

import com.example.android.nfc.record.{ParsedNdefRecord, SmartPoster, TextRecord, UriRecord}


/**
 * Utility class for creating {@link ParsedNdefMessage}s.
 */
object NdefMessageParser {

  /** Parse an NdefMessage */
  def parse(message: NdefMessage): List[ParsedNdefRecord] =
    getRecords(message.getRecords)

  def getRecords(records: Array[NdefRecord]): List[ParsedNdefRecord] = {
    val elements = new ListBuffer[ParsedNdefRecord]
    for (record <- records) {
      if (UriRecord.isUri(record))
        elements += UriRecord.parse(record)
      else if (TextRecord.isText(record))
        elements += TextRecord.parse(record)
      else if (SmartPoster.isPoster(record))
        elements += SmartPoster.parse(record)
    }
    elements.toList
  }
}
