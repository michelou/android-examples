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
import android.nfc.{FormatException, NdefMessage, NdefRecord}
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout

import com.google.common.base.{Charsets, Preconditions}

import com.example.android.nfc.NdefMessageParser
import com.example.android.nfc.R

import SmartPoster.RecommendedAction

/**
 * A representation of an NFC Forum "Smart Poster".
 */
class SmartPoster(uri: UriRecord, title: TextRecord,
                  action: RecommendedAction, typ: String) extends ParsedNdefRecord {

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Title record for the service (there can be many of these in
   * different languages, but a language MUST NOT be repeated). This record is
   * optional."
   */
  private final val mTitleRecord = title

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The URI record. This is the core of the Smart Poster, and all other
   * records are just metadata about this record. There MUST be one URI record
   * and there MUST NOT be more than one."
   */
  private final val mUriRecord = Preconditions.checkNotNull(uri)

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Action record. This record describes how the service should be
   * treated. For example, the action may indicate that the device should save
   * the URI as a bookmark or open a browser. The Action record is optional.
   * If it does not exist, the device may decide what to do with the service.
   * If the action record exists, it should be treated as a strong suggestion;
   * the UI designer may ignore it, but doing so will induce a different user
   * experience from device to device."
   */
  private final val mAction = Preconditions.checkNotNull(action)

  /**
   * NFC Forum Smart Poster Record Type Definition section 3.2.1.
   *
   * "The Type record. If the URI references an external entity (e.g., via a
   * URL), the Type record may be used to declare the MIME type of the entity.
   * This can be used to tell the mobile device what kind of an object it can
   * expect before it opens the connection. The Type record is optional."
   */
  private final val mType = typ

  def getUriRecord: UriRecord = mUriRecord

  /**
   * Returns the title of the smart poster. This may be {@code null}.
   */
  def getTitle: TextRecord = mTitleRecord

  def getView(activity: Activity, inflater: LayoutInflater,
              parent: ViewGroup, offset: Int): View =
    if (mTitleRecord != null) {
      // Build a container to hold the title and the URI
      val container = new LinearLayout(activity)
      container setOrientation LinearLayout.VERTICAL
      container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT))
      container.addView(mTitleRecord.getView(activity, inflater, container, offset))
      inflater.inflate(R.layout.tag_divider, container)
      container.addView(mUriRecord.getView(activity, inflater, container, offset))
      container
    } else {
      // Just a URI, return a view for it directly
      mUriRecord.getView(activity, inflater, parent, offset)
    }
}
  
object SmartPoster {

  def parse(record: NdefRecord): SmartPoster = {
    Preconditions.checkArgument(record.getTnf == NdefRecord.TNF_WELL_KNOWN)
    Preconditions.checkArgument(record.getType == NdefRecord.RTD_SMART_POSTER)
    try {
      val subRecords = new NdefMessage(record.getPayload)
      parse(subRecords.getRecords)
    } catch {
      case e: FormatException =>
        throw new IllegalArgumentException(e)
    }
  }

  def parse(recordsRaw: Array[NdefRecord]): SmartPoster =
    try {
      val records = NdefMessageParser.getRecords(recordsRaw)
      val uri = getFirstIfExists[UriRecord](records)
      val title = getFirstIfExists[TextRecord](records)
      val action = parseRecommendedAction(recordsRaw)
      val typ = parseType(recordsRaw)
      new SmartPoster(uri, title, action, typ)
    } catch {
      case e: NoSuchElementException =>
        throw new IllegalArgumentException(e)
    }

  def isPoster(record: NdefRecord): Boolean =
    try { parse(record); true }
    catch { case e: IllegalArgumentException => false }

  /**
   * Returns the first element of {@code elements} which is an instance of
   * {@code type}, or {@code null} if no such element exists.
   */
  private def getFirstIfExists[T](elements: Iterable[AnyRef]): T =
    (elements find (_.isInstanceOf[T]) match {
      case Some(filtered) => filtered
      case None => null
    }).asInstanceOf[T]

  object RecommendedAction extends Enumeration {
    val UNKNOWN, DO_ACTION, SAVE_FOR_LATER, OPEN_FOR_EDITING = Value
    val LOOKUP = Map(
      -1.toByte -> UNKNOWN, 0.toByte -> DO_ACTION,
      1.toByte -> SAVE_FOR_LATER, 2.toByte -> OPEN_FOR_EDITING
    )
  }
  type RecommendedAction = RecommendedAction.Value

  private def getByType(typ: Array[Byte], records: Array[NdefRecord]): NdefRecord = {
    for (record <- records) {
      if (typ == record.getType) return record
    }
    null
  }

  private final val ACTION_RECORD_TYPE = Array[Byte]('a', 'c', 't')

  private def parseRecommendedAction(records: Array[NdefRecord]): RecommendedAction = {
    val record = getByType(ACTION_RECORD_TYPE, records)
    if (record == null)
      RecommendedAction.UNKNOWN
    else {
      val action = record.getPayload.head
      RecommendedAction.LOOKUP get action match {
        case Some(elem) => elem
	case None =>  RecommendedAction.UNKNOWN
      }
    }
  }

  private final val TYPE_TYPE = Array[Byte]('t')

  private def parseType(records: Array[NdefRecord]): String = {
    val typ = getByType(TYPE_TYPE, records)
    if (typ == null) null
    else new String(typ.getPayload, Charsets.UTF_8)
  }
}
