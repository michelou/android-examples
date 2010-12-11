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
import android.widget.{ArrayAdapter, AutoCompleteTextView}

class HelloAutoComplete extends Activity {
  /** Called when the activity is first created. */
  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.autocomplete)

    val textView = findViewById(R.id.autocomplete_country).asInstanceOf[AutoCompleteTextView]
    val countries = getResources getStringArray R.array.countries_array
    val adapter = new ArrayAdapter[String](this, R.layout.list_item, countries)
    textView setAdapter adapter
  }

}
