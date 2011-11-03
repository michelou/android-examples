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

package com.example.android.supportv4.app

import com.example.android.supportv4.R

import android.os.Bundle
import android.support.v4.app.{DialogFragment, FragmentActivity, FragmentTransaction}
import android.view.LayoutInflater
import android.view.{View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

class FragmentDialogOrActivitySupport extends FragmentActivity {
  import FragmentDialogOrActivitySupport._

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_dialog_or_activity)

    if (savedInstanceState == null) {
      // First-time init; create fragment to embed in activity.

      val ft = getSupportFragmentManager().beginTransaction()
      val newFragment = MyDialogFragment.newInstance()
      ft.add(R.id.embedded, newFragment)
      ft.commit()
    }

    // Watch for button clicks.
    val button = findViewById(R.id.show_dialog).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { showDialog() }
    }
  }

  def showDialog() {
    // Create the fragment and show it as a dialog.
    val newFragment = MyDialogFragment.newInstance()
    newFragment.show(getSupportFragmentManager, "dialog")
  }

}

object FragmentDialogOrActivitySupport {

  private object MyDialogFragment {
    def newInstance(): MyDialogFragment = new MyDialogFragment()
  }

  private class MyDialogFragment extends DialogFragment {
    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.hello_world, container, false)
      val tv = v.findViewById(R.id.text).asInstanceOf[TextView]
      tv setText "This is an instance of MyDialogFragment"
      v
    }
  }

}
