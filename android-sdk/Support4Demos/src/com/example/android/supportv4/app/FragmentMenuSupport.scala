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
import android.support.v4.view.MenuCompat

import android.os.Bundle
import android.view.{Menu, MenuInflater, MenuItem, View}
import android.view.View.OnClickListener
import android.widget.CheckBox

/**
 * Demonstrates how fragments can participate in the options menu.
 */
class FragmentMenuSupport extends FragmentActivity {
  import FragmentMenuSupport._  // companion object

  private var mFragment1: Fragment = _
  private var mFragment2: Fragment = _
  private var mCheckBox1: CheckBox = _
  private var mCheckBox2: CheckBox = _

  // Update fragment visibility when check boxes are changed.
  private val mClickListener = new OnClickListener() {
    def onClick(v: View) { updateFragmentVisibility() }
  }

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_menu)

    // Make sure the two menu fragments are created.
    val fm = getSupportFragmentManager()
    val ft = fm.beginTransaction()
    mFragment1 = fm findFragmentByTag "f1"
    if (mFragment1 == null) {
      mFragment1 = new MenuFragment()
      ft.add(mFragment1, "f1")
    }
    mFragment2 = fm findFragmentByTag "f2"
    if (mFragment2 == null) {
      mFragment2 = new Menu2Fragment()
      ft.add(mFragment2, "f2")
    }
    ft.commit()

    // Watch check box clicks.
    mCheckBox1 = findViewById(R.id.menu1).asInstanceOf[CheckBox]
    mCheckBox1 setOnClickListener mClickListener
    mCheckBox2 = findViewById(R.id.menu2).asInstanceOf[CheckBox]
    mCheckBox2 setOnClickListener mClickListener

    // Make sure fragments start out with correct visibility.
    updateFragmentVisibility()
  }

  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    // Make sure fragments are updated after check box view state is restored.
    updateFragmentVisibility()
  }

  // Update fragment visibility based on current check box state.
  private def updateFragmentVisibility() {
    val ft = getSupportFragmentManager().beginTransaction();
    if (mCheckBox1.isChecked) ft show mFragment1
    else ft hide mFragment1
    if (mCheckBox2.isChecked) ft show mFragment2
    else ft hide mFragment2
    ft.commit()
  }

}

object FragmentMenuSupport {

  /**
   * A fragment that displays a menu.  This fragment happens to not
   * have a UI (it does not implement onCreateView), but it could also
   * have one if it wanted.
   */
  private class MenuFragment extends Fragment {

    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      setHasOptionsMenu(true)
    }

    override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      var item = menu.add("Menu 1a")
      //MenuCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_IF_ROOM) // API 11
      item = menu.add("Menu 1b")
      //MenuCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_IF_ROOM) // API 11
    }
  }

  /**
   * Second fragment with a menu.
   */
  private class Menu2Fragment extends Fragment {

    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)
      setHasOptionsMenu(true)
    }

    override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      var item = menu.add("Menu 2")
      //MenuCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_IF_ROOM) // API 11
    }
  }
}
