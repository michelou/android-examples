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

import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager, FragmentTransaction}

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

/**
 * Demonstration of hiding and showing fragments.
 */
class FragmentHideShowSupport extends FragmentActivity {
  import FragmentHideShowSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_hide_show_support)

    // The content view embeds two fragments; now retrieve them and attach
    // their "hide" button.
    val fm = getSupportFragmentManager
    addShowHideListener(R.id.frag1hide, fm findFragmentById R.id.fragment1)
    addShowHideListener(R.id.frag2hide, fm findFragmentById R.id.fragment2)
  }

  def addShowHideListener(buttonId: Int, fragment: Fragment) {
    val button = findViewById(buttonId).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        val ft = getSupportFragmentManager.beginTransaction()
        ft.setCustomAnimations(android.R.anim.fade_in,
                               android.R.anim.fade_out)
        if (fragment.isHidden) {
          ft show fragment
          button setText "Hide"
        } else {
          ft hide fragment
          button setText "Show"
        }
        ft.commit()
      }
    }
  }

}

object FragmentHideShowSupport {

  private class FirstFragment extends Fragment {
    private var mTextView: TextView = _

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.labeled_text_edit, container, false)
      val tv = v.findViewById(R.id.msg).asInstanceOf[TextView]
      tv setText "The fragment saves and restores this text."

      // Retrieve the text editor, and restore the last saved state if needed.
      mTextView = v.findViewById(R.id.saved).asInstanceOf[TextView]
      if (savedInstanceState != null) {
        mTextView setText savedInstanceState.getCharSequence("text")
      }
      v
    }

    override def onSaveInstanceState(outState: Bundle) {
      super.onSaveInstanceState(outState)

      // Remember the current text, to restore if we later restart.
      outState.putCharSequence("text", mTextView.getText)
    }
  }

  private class SecondFragment extends Fragment {

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.labeled_text_edit, container, false)
      val tv = v.findViewById(R.id.msg).asInstanceOf[TextView]
      tv setText "The TextView saves and restores this text."

      // Retrieve the text editor and tell it to save and restore its state.
      // Note that you will often set this in the layout XML, but since
      // we are sharing our layout with the other fragment we will customize
      // it here.
      v.findViewById(R.id.saved).asInstanceOf[TextView] setSaveEnabled true
      v
    }
  }

}
