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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

class HelloViews extends Activity {

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    setOnClickAction(R.id.start_gridview, classOf[HelloGridView])
    setOnClickAction(R.id.start_linearlayout, classOf[HelloLinearLayout])
    setOnClickAction(R.id.start_relativelayout, classOf[HelloRelativeLayout])
    setOnClickAction(R.id.start_tablelayout, classOf[HelloTableLayout])
    setOnClickAction(R.id.start_tabwidget, classOf[HelloTabWidget])
    setOnClickAction(R.id.start_datepicker, classOf[HelloDatePicker])
    setOnClickAction(R.id.start_timepicker, classOf[HelloTimePicker])
    setOnClickAction(R.id.start_spinner, classOf[HelloSpinner])
    setOnClickAction(R.id.start_autocomplete, classOf[HelloAutoComplete])
  }

  private def setOnClickAction(id: Int, cls: Class[_]) {
    findViewById(id) setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(HelloViews.this, cls))
      }
    }
  }

}
