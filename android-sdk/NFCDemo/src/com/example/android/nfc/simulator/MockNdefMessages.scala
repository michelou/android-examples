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
package com.example.android.nfc.simulator

/**
 * This class provides a list of fake NFC Ndef format Tags.
 */
object MockNdefMessages {

  /**
   * A Smart Poster containing a URL and no text.
   */
  final val SMART_POSTER_URL_NO_TEXT = Array[Byte](
    0xd1.toByte, 0x02, 0x0f, 0x53, 0x70, 0xd1.toByte,
    0x01, 0x0b, 0x55, 0x01, 0x67, 0x6f,
    0x6f, 0x67, 0x6c, 0x65, 0x2e, 0x63,
    0x6f, 0x6d
  )

  /**
   * A plain text tag in english.
   */
  final val ENGLISH_PLAIN_TEXT = Array[Byte](
    0xd1.toByte, 0x01, 0x1c, 0x54, 0x02, 0x65,
    0x6e, 0x53, 0x6f, 0x6d, 0x65, 0x20,
    0x72, 0x61, 0x6e, 0x64, 0x6f, 0x6d,
    0x20, 0x65, 0x6e, 0x67, 0x6c, 0x69,
    0x73, 0x68, 0x20, 0x74, 0x65, 0x78,
    0x74, 0x2e
  )

  /**
   * Smart Poster containing a URL and Text.
   */
  final val SMART_POSTER_URL_AND_TEXT = Array[Byte](
    0xd1.toByte, 0x02, 0x1c, 0x53, 0x70, 0x91.toByte,
    0x01, 0x09, 0x54, 0x02, 0x65, 0x6e,
    0x47, 0x6f, 0x6f, 0x67, 0x6c, 0x65,
    0x51, 0x01, 0x0b, 0x55, 0x01, 0x67,
    0x6f, 0x6f, 0x67, 0x6c, 0x65, 0x2e,
    0x63, 0x6f, 0x6d
  )

  /**
   * All the mock Ndef tags.
   */
  final val ALL_MOCK_MESSAGES = Array(
    SMART_POSTER_URL_NO_TEXT, ENGLISH_PLAIN_TEXT, SMART_POSTER_URL_AND_TEXT
  )
}
