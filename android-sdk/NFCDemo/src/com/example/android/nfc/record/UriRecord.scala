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
package com.example.android.nfc.record

import android.app.Activity
import android.net.Uri
import android.nfc.NdefRecord
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.TextView

import com.google.common.base.{Charsets, Preconditions}

import com.example.android.nfc.R

/**
 * A parsed record containing a Uri.
 */
class UriRecord private (uri: Uri) extends ParsedNdefRecord {
  import UriRecord._  // companion object

  private val mUri = Preconditions.checkNotNull(uri)

  def getView(activity: Activity, inflater: LayoutInflater, parent: ViewGroup, offset: Int): View = {
    val text = inflater.inflate(R.layout.tag_text, parent, false).asInstanceOf[TextView]
    text setText mUri.toString
    text
  }

  def getUri: Uri = mUri

}

object UriRecord {

  private final val TAG = "UriRecord"

  final val RECORD_TYPE = "UriRecord"

  /**
   * NFC Forum "URI Record Type Definition"
   *
   * This is a mapping of "URI Identifier Codes" to URI string prefixes,
   * per section 3.2.2 of the NFC Forum URI Record Type Definition document.
   */
  private final val URI_PREFIX_MAP = Map[Byte, String](
    0x00.toByte -> "",
    0x01.toByte -> "http://www.",
    0x02.toByte -> "https://www.",
    0x03.toByte -> "http://",
    0x04.toByte -> "https://",
    0x05.toByte -> "tel:",
    0x06.toByte -> "mailto:",
    0x07.toByte -> "ftp://anonymous:anonymous@",
    0x08.toByte -> "ftp://ftp.",
    0x09.toByte -> "ftps://",
    0x0A.toByte -> "sftp://",
    0x0B.toByte -> "smb://",
    0x0C.toByte -> "nfs://",
    0x0D.toByte -> "ftp://",
    0x0E.toByte -> "dav://",
    0x0F.toByte -> "news:",
    0x10.toByte -> "telnet://",
    0x11.toByte -> "imap:",
    0x12.toByte -> "rtsp://",
    0x13.toByte -> "urn:",
    0x14.toByte -> "pop:",
    0x15.toByte -> "sip:",
    0x16.toByte -> "sips:",
    0x17.toByte -> "tftp:",
    0x18.toByte -> "btspp://",
    0x19.toByte -> "btl2cap://",
    0x1A.toByte -> "btgoep://",
    0x1B.toByte -> "tcpobex://",
    0x1C.toByte -> "irdaobex://",
    0x1D.toByte -> "file://",
    0x1E.toByte -> "urn:epc:id:",
    0x1F.toByte -> "urn:epc:tag:",
    0x20.toByte -> "urn:epc:pat:",
    0x21.toByte -> "urn:epc:raw:",
    0x22.toByte -> "urn:epc:",
    0x23.toByte -> "urn:nfc:"
  )

  /**
   * Convert {@link android.nfc.NdefRecord} into a {@link android.net.Uri}.
   * This will handle both TNF_WELL_KNOWN / RTD_URI and TNF_ABSOLUTE_URI.
   *
   * @throws IllegalArgumentException if the NdefRecord is not a record
   *         containing a URI.
   */
  def parse(record: NdefRecord): UriRecord = {
    val tnf = record.getTnf
    if (tnf == NdefRecord.TNF_WELL_KNOWN)
      parseWellKnown(record)
    else if (tnf == NdefRecord.TNF_ABSOLUTE_URI)
      parseAbsolute(record)
    else
      throw new IllegalArgumentException("Unknown TNF " + tnf)
  }

  /** Parse and absolute URI record */
  private def parseAbsolute(record: NdefRecord): UriRecord = {
    val payload = record.getPayload
    val uri = Uri.parse(new String(payload, Charsets.UTF_8))
    new UriRecord(uri)
  }

  /** Parse an well known URI record */
  private def parseWellKnown(record: NdefRecord): UriRecord = {
    Preconditions.checkArgument(record.getType == NdefRecord.RTD_URI)
    val payload = record.getPayload
    /*
     * payload[0] contains the URI Identifier Code, per the
     * NFC Forum "URI Record Type Definition" section 3.2.2.
     *
     * payload[1]...payload[payload.length - 1] contains the rest of
     * the URI.
     */
    val prefix = URI_PREFIX_MAP(payload.head)
    val fullUri = prefix.getBytes(Charsets.UTF_8) ++ payload.tail
    val uri = Uri.parse(new String(fullUri, Charsets.UTF_8))
    new UriRecord(uri)
  }

  def isUri(record: NdefRecord): Boolean =
    try { parse(record); true }
    catch { case e: IllegalArgumentException => false }

  private final val EMPTY = Array[Byte]()
}
