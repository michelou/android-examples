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

import android.support.v4.app.{Fragment, FragmentActivity, FragmentTransaction}

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, FrameLayout, TextView}

class FragmentReceiveResultSupport extends FragmentActivity {
  import FragmentReceiveResultSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    val lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
    val frame = new FrameLayout(this)
    frame setId R.id.simple_fragment
    setContentView(frame, lp)

    if (savedInstanceState == null) {
      // Do first time initialization -- add fragment.
      val newFragment = new ReceiveResultFragment()
      val ft = getSupportFragmentManager.beginTransaction()
      ft.add(R.id.simple_fragment, newFragment).commit()
    }
  }

}

object FragmentReceiveResultSupport {

  private object ReceiveResultFragment {
    // Definition of the one requestCode we use for receiving resuls.
    final private val GET_CODE = 0
  }

  private class ReceiveResultFragment extends Fragment {
    import ReceiveResultFragment._  // companion object

    private var mResults: TextView = _

    private val mGetListener = new OnClickListener() {
      def onClick(v: View) {
        // Start the activity whose result we want to retrieve.  The
        // result will come back with request code GET_CODE.
        val intent = new Intent(getActivity, classOf[SendResult])
        startActivityForResult(intent, GET_CODE)
      }
    }

    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
    }

    override def onSaveInstanceState(outState: Bundle) {
      super.onSaveInstanceState(outState)
    }

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.receive_result, container, false)

      // Retrieve the TextView widget that will display results.
      mResults = v.findViewById(R.id.results).asInstanceOf[TextView]

      // This allows us to later extend the text buffer.
      mResults.setText(mResults.getText, TextView.BufferType.EDITABLE)

      // Watch for button clicks.
      val getButton = v.findViewById(R.id.get).asInstanceOf[Button]
      getButton setOnClickListener mGetListener

      v
    }

    /**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     */
    override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
      // You can use the requestCode to select between multiple child
      // activities you may have started.  Here there is only one thing
      // we launch.
      if (requestCode == GET_CODE) {

        // We will be adding to our text.
        val text = mResults.getText.asInstanceOf[Editable]

        // This is a standard resultCode that is sent back if the
        // activity doesn't supply an explicit result.  It will also
        // be returned if the activity failed to launch.
        if (resultCode == Activity.RESULT_CANCELED) {
          text append "(cancelled)"

          // Our protocol with the sending activity is that it will send
          // text in 'data' as its result.
        } else {
          text append "(okay "
          text append resultCode.toString
          text append ") "
          if (data != null) {
            text append data.getAction
          }
        }

        text append "\n"
      }
    }
  }
}
