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

import android.support.v4.app.{DialogFragment, Fragment, FragmentActivity, FragmentTransaction}

import android.os.Bundle
import android.view.LayoutInflater
import android.view.{View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

class FragmentDialogSupport extends FragmentActivity {
  import FragmentDialogSupport._

  private var mStackLevel = 0

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_dialog)

    val tv = findViewById(R.id.text).asInstanceOf[TextView]
    tv setText ("Example of displaying dialogs with a DialogFragment.  "
                + "Press the show button below to see the first dialog; pressing "
                + "successive show buttons will display other dialog styles as a "
                + "stack, with dismissing or back going to the previous dialog.")

    // Watch for button clicks.
    val button = findViewById(R.id.show).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { showDialog() }
    }

    if (savedInstanceState != null) {
      mStackLevel = savedInstanceState getInt "level"
    }
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt("level", mStackLevel)
  }


  def showDialog() {
    mStackLevel += 1

    // DialogFragment.show() will take care of adding the fragment
    // in a transaction.  We also want to remove any currently showing
    // dialog, so make our own transaction and take care of that here.
    val ft = getSupportFragmentManager.beginTransaction()
    val prev = getSupportFragmentManager findFragmentByTag "dialog"
    if (prev != null) ft remove prev
    ft addToBackStack null

    // Create and show the dialog.
    val newFragment = MyDialogFragment.newInstance(mStackLevel)
    newFragment.show(ft, "dialog")
  }
}

object FragmentDialogSupport {

  private def getNameForNum(num: Int): String =
    (num-1) % 6 match {
      case 1 => "STYLE_NO_TITLE"
      case 2 => "STYLE_NO_FRAME"
      case 3 => "STYLE_NO_INPUT (this window can't receive input, so " +
                "you will need to press the bottom show button)"
      case 4 => "STYLE_NORMAL with dark fullscreen theme"
      case 5 => "STYLE_NORMAL with light theme"
      case 6 => "STYLE_NO_TITLE with light theme"
      case 7 => "STYLE_NO_FRAME with light theme"
      case 8 => "STYLE_NORMAL with light fullscreen theme"
      case _ => "STYLE_NORMAL"
    }

  private object MyDialogFragment {
    /**
     * Create a new instance of MyDialogFragment, providing "num"
     * as an argument.
     */
    def newInstance(num: Int): MyDialogFragment = {
      val f = new MyDialogFragment()

      // Supply num input as an argument.
      val args = new Bundle()
      args.putInt("num", num)
      f setArguments args
      f
    }
  }

  private class MyDialogFragment extends DialogFragment {
    private var mNum: Int = _

    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      mNum = getArguments getInt "num"

      // Pick a style based on the num.
      val style = (mNum-1) % 6 match {
        case 1 => DialogFragment.STYLE_NO_TITLE
        case 2 => DialogFragment.STYLE_NO_FRAME
        case 3 => DialogFragment.STYLE_NO_INPUT
        case 4 => DialogFragment.STYLE_NORMAL
        case 5 => DialogFragment.STYLE_NO_TITLE
        case 6 => DialogFragment.STYLE_NO_FRAME
        case 7 => DialogFragment.STYLE_NORMAL
        case _ => DialogFragment.STYLE_NORMAL
      }
      val theme = (mNum-1) % 6 match {
        case 2 => android.R.style.Theme_Panel
        case 4 => android.R.style.Theme
        case 5 => android.R.style.Theme_Light
        case 6 => android.R.style.Theme_Light_Panel
        case 7 => android.R.style.Theme_Light
        case _ => 0
      }
      setStyle(style, theme)
    }

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.fragment_dialog, container, false)
      val tv = v.findViewById(R.id.text).asInstanceOf[TextView]
      tv.setText("Dialog #" + mNum + ": using style " + getNameForNum(mNum))

      // Watch for button clicks.
      val button = v.findViewById(R.id.show).asInstanceOf[Button]
      button setOnClickListener new OnClickListener() {
        def onClick(v: View ) {
          // When button is clicked, call up to owning activity.
          getActivity.asInstanceOf[FragmentDialogSupport].showDialog();
        }
      }

      v
    }
  }

}
