/*
 * Copyright (C) 2011 The Android Open Source Project
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

import android.support.v4.app.{DialogFragment, FragmentActivity}

import android.app.{AlertDialog, Dialog}
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

/**
 * Demonstrates how to show an AlertDialog that is managed by a Fragment.
 */
class FragmentAlertDialogSupport extends FragmentActivity {
  import FragmentAlertDialogSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_dialog)

    val tv = findViewById(R.id.text).asInstanceOf[TextView]
    tv setText "Example of displaying an alert dialog with a DialogFragment"

    // Watch for button clicks.
    val button = findViewById(R.id.show).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { showDialog() }
    }
  }

  def showDialog() {
    val newFragment = MyAlertDialogFragment.newInstance(
      R.string.alert_dialog_two_buttons_title)
    newFragment.show(getSupportFragmentManager, "dialog")
  }

  def doPositiveClick() {
    // Do stuff here.
    Log.i("FragmentAlertDialog", "Positive click!")
  }

  def doNegativeClick() {
    // Do stuff here.
    Log.i("FragmentAlertDialog", "Negative click!")
  }

}

object FragmentAlertDialogSupport {

  object MyAlertDialogFragment {
    def newInstance(title: Int): MyAlertDialogFragment = {
      val frag = new MyAlertDialogFragment()
      val args = new Bundle()
      args.putInt("title", title)
      frag setArguments args
      frag
    }
  }

  class MyAlertDialogFragment extends DialogFragment {
    override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
      val title = getArguments getInt "title"

      new AlertDialog.Builder(getActivity)
        .setIcon(R.drawable.alert_dialog_icon)
        .setTitle(title)
        .setPositiveButton(R.string.alert_dialog_ok,
          new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, whichButton: Int) {
              getActivity.asInstanceOf[FragmentAlertDialogSupport].doPositiveClick()
            }
          }
        )
        .setNegativeButton(R.string.alert_dialog_cancel,
          new DialogInterface.OnClickListener() {
            def onClick(dialog: DialogInterface, whichButton: Int) {
              getActivity.asInstanceOf[FragmentAlertDialogSupport].doNegativeClick()
            }
          }
        )
        .create()
    }
  }

}
