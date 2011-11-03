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

import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager,
                               FragmentStatePagerAdapter, ListFragment}
import android.support.v4.view.ViewPager

import android.os.Bundle
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, ListView, TextView}

import com.example.android.supportv4.Cheeses
import com.example.android.supportv4.R

class FragmentStatePagerSupport extends FragmentActivity {
  import FragmentStatePagerSupport._  // companion object

  private var mAdapter: MyAdapter = _
  private var mPager: ViewPager = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_pager)

    mAdapter = new MyAdapter(getSupportFragmentManager)

    mPager = findViewById(R.id.pager).asInstanceOf[ViewPager]
    mPager setAdapter mAdapter

    // Watch for button clicks.
    var button = findViewById(R.id.goto_first).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { mPager setCurrentItem 0 }
    }
    button = findViewById(R.id.goto_last).asInstanceOf[Button]
    button setOnClickListener new OnClickListener() {
      def onClick(v: View) { mPager setCurrentItem (NUM_ITEMS-1) }
    }
  }

}

object FragmentStatePagerSupport {
  private final val NUM_ITEMS = 10

  private class MyAdapter(fm: FragmentManager) extends FragmentStatePagerAdapter(fm) {
    override def getCount: Int = NUM_ITEMS
    override def getItem(position: Int): Fragment =
      ArrayListFragment.newInstance(position)
  }

  object ArrayListFragment {

    /**
     * Create a new instance of CountingFragment, providing "num"
     * as an argument.
     */
    def newInstance(num: Int): ArrayListFragment = {
      val f = new ArrayListFragment()

      // Supply num input as an argument.
      val args = new Bundle()
      args.putInt("num", num)
      f setArguments args

      f
    }
  }

  private[app] class ArrayListFragment extends ListFragment {
    private var mNum: Int = _

    /**
     * When creating, retrieve this instance's number from its arguments.
     */
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState);
      mNum = if (getArguments != null) getArguments getInt "num" else 1
    }

    /**
     * The Fragment's UI is just a simple text view showing its
     * instance number.
     */
    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.fragment_pager_list, container, false)
      val tv = v.findViewById(R.id.text).asInstanceOf[TextView]
      tv setText ("Fragment #" + mNum)
      v
    }

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)
      setListAdapter(new ArrayAdapter[String](getActivity,
                    android.R.layout.simple_list_item_1, Cheeses.sCheeseStrings))
    }

    override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
      Log.i("FragmentList", "Item clicked: " + id)
    }
  }
}
