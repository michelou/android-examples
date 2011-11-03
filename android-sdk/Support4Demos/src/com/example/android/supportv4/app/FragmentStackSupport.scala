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

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

class FragmentStackSupport extends FragmentActivity {
  import FragmentStackSupport._  // companion object

  private var mStackLevel = 1

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_stack)

    // Watch for button clicks.
    val button = findViewById(R.id.new_fragment).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { addFragmentToStack() }
    }

    if (savedInstanceState == null) {
      // Do first time initialization -- add initial fragment.
      val newFragment = CountingFragment.newInstance(mStackLevel)
      val ft = getSupportFragmentManager.beginTransaction()
      ft.add(R.id.simple_fragment, newFragment).commit()
    } else {
      mStackLevel = savedInstanceState getInt "level"
    }
  }

  override def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putInt("level", mStackLevel)
  }


  private def addFragmentToStack() {
    mStackLevel += 1

    // Instantiate a new fragment.
    val newFragment = CountingFragment.newInstance(mStackLevel)

    // Add the fragment to the activity, pushing this transaction
    // on to the back stack.
    val ft = getSupportFragmentManager.beginTransaction()
    ft.replace(R.id.simple_fragment, newFragment)
    ft setTransition FragmentTransaction.TRANSIT_FRAGMENT_OPEN
    ft addToBackStack null
    ft.commit()
  }

}

object FragmentStackSupport {

  private object CountingFragment {

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    def newInstance(num: Int): CountingFragment = {
      val f = new CountingFragment()

      // Supply num input as an argument.
      val args = new Bundle()
      args.putInt("num", num)
      f setArguments args

      f
    }

  }

  private[app] class CountingFragment extends Fragment {
    private var mNum: Int = _

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      mNum = if (getArguments != null) getArguments getInt "num" else 1
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.hello_world, container, false)
      val tv = v.findViewById(R.id.text).asInstanceOf[TextView]
      tv setText ("Fragment #" + mNum)
      tv setBackgroundDrawable getResources.getDrawable(android.R.drawable.gallery_thumb)
      v
    }
  }

}
