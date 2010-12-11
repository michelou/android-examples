/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.helloviews

import android.app.{Activity, Dialog, TimePickerDialog}
import android.os.Bundle
import android.view.View
import android.widget.{Button, TextView, TimePicker}

import java.util.Calendar

class HelloTimePicker extends Activity {
  import HelloTimePicker._  // companion object

  private var mTimeDisplay: TextView = _
  private var mPickTime: Button = _

  private var mHour: Int = _
  private var mMinute: Int = _

  /** Called when the activity is first created. */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.timepicker)

    // capture our View elements
    mTimeDisplay = findViewById(R.id.timeDisplay).asInstanceOf[TextView]
    mPickTime = findViewById(R.id.pickTime).asInstanceOf[Button]

    // add a click listener to the button
    mPickTime setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(TIME_DIALOG_ID)
      }
    }

    // get the current time
    val c = Calendar.getInstance
    mHour = c get Calendar.HOUR_OF_DAY
    mMinute = c get Calendar.MINUTE

    // display the current date
    updateDisplay()
  }

  // updates the time we display in the TextView
  private def updateDisplay() {
    mTimeDisplay setText
             new StringBuilder()
                    .append(pad(mHour)).append(":")
                    .append(pad(mMinute))
  }

  // the callback received when the user "sets" the time in the dialog
  private val mTimeSetListener =
    new TimePickerDialog.OnTimeSetListener() {
      def onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        mHour = hourOfDay
        mMinute = minute
        updateDisplay()
      }
    }

  override protected def onCreateDialog(id: Int): Dialog =
    id match {
      case TIME_DIALOG_ID =>
        new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, false)
      case _ =>
        null
    }

}

object HelloTimePicker {
  final val TIME_DIALOG_ID = 0

  private def pad(c: Int): String =
    (if (c >= 10) "" else "0") + String.valueOf(c)

}
