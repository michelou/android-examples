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

import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentActivity}
import android.util.Log
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.{LayoutInflater, Menu, MenuItem, View, ViewGroup}

/**
 * Demonstration of displaying a context menu from a fragment.
 */
class FragmentContextMenuSupport extends FragmentActivity {
  import FragmentContextMenuSupport._

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Create the list fragment and add it as our sole content.
    val content = new ContextMenuFragment()
    getSupportFragmentManager
      .beginTransaction()
      .add(android.R.id.content, content)
      .commit()
  }

}

object FragmentContextMenuSupport {

  private class ContextMenuFragment extends Fragment {

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val root = inflater.inflate(R.layout.fragment_context_menu, container, false)
      registerForContextMenu(root.findViewById(R.id.long_press))
      root
    }

    override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo)
      menu.add(Menu.NONE, R.id.a_item, Menu.NONE, "Menu A")
      menu.add(Menu.NONE, R.id.b_item, Menu.NONE, "Menu B")
    }

    override def onContextItemSelected(item: MenuItem): Boolean =
      item.getItemId match {
        case R.id.a_item =>
          Log.i("ContextMenu", "Item 1a was chosen")
          true
        case R.id.b_item =>
          Log.i("ContextMenu", "Item 1b was chosen")
          true
        case _ =>
          super.onContextItemSelected(item)
      }
  }

}
