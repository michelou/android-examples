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
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ArrayAdapter, Spinner, Toast}

class HelloSpinner extends Activity {

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.spinner)

    val spinner = findViewById(R.id.spinner).asInstanceOf[Spinner]
    val adapter = ArrayAdapter.createFromResource(
                this, R.array.planets_array, android.R.layout.simple_spinner_item)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner setAdapter adapter

    spinner setOnItemSelectedListener new MyOnItemSelectedListener()
  }

  private class MyOnItemSelectedListener extends AdapterView.OnItemSelectedListener {

    def onItemSelected(parent: AdapterView[_], view: View, pos: Int, id: Long) {
      val msg = getResources getString R.string.planet_message
      Toast.makeText(parent.getContext, msg + " " +
                parent.getItemAtPosition(pos).toString, Toast.LENGTH_LONG).show()
    }

    def onNothingSelected(parent: AdapterView[_]) {
      // Do nothing.
    }
  }

}
