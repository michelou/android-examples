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

import android.app.{Activity, DatePickerDialog, Dialog}
import android.os.Bundle
import android.view.View
import android.widget.{Button, DatePicker, TextView}

import java.util.Calendar

class HelloDatePicker extends Activity {
  import HelloDatePicker._  // companion object

  private var mDateDisplay: TextView = _
  private var mPickDate: Button = _
  private var mYear: Int = _
  private var mMonth: Int = _
  private var mDay: Int = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.datepicker)

    // capture our View elements
    mDateDisplay = findViewById(R.id.dateDisplay).asInstanceOf[TextView]
    mPickDate = findViewById(R.id.pickDate).asInstanceOf[Button]

    // add a click listener to the button
    mPickDate setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        showDialog(DATE_DIALOG_ID)
      }
    }

    // get the current date
    val c = Calendar.getInstance
    mYear = c get Calendar.YEAR
    mMonth = c get Calendar.MONTH
    mDay = c get Calendar.DAY_OF_MONTH

    // display the current date (this method is below)
    updateDisplay()
  }

  // updates the date in the TextView
  private def updateDisplay() {
    mDateDisplay setText
            new StringBuilder()
                    // Month is 0 based so add 1
                    .append(mMonth + 1).append("-")
                    .append(mDay).append("-")
                    .append(mYear).append(" ")
  }

  // the callback received when the user "sets" the date in the dialog
  private val mDateSetListener = new DatePickerDialog.OnDateSetListener() {
    def onDateSet(view: DatePicker, year: Int, 
                  monthOfYear: Int, dayOfMonth: Int) {
        mYear = year
        mMonth = monthOfYear
        mDay = dayOfMonth
        updateDisplay()
      }
   }

  override protected def onCreateDialog(id: Int): Dialog =
    id match {
      case DATE_DIALOG_ID =>
        new DatePickerDialog(this, mDateSetListener, mYear, mMonth, mDay)
      case _ =>
        null
    }
}

object HelloDatePicker {
  final val DATE_DIALOG_ID = 0
}
