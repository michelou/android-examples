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

import com.example.android.supportv4.Shakespeare
import com.example.android.supportv4.R

import android.support.v4.app.{Fragment, FragmentActivity, FragmentTransaction, ListFragment}

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{ArrayAdapter, ListView, ScrollView, TextView}

/**
 * Demonstration of using fragments to implement different activity layouts.
 * This sample provides a different layout (and activity flow) when run in
 * landscape.
 */
class FragmentLayoutSupport extends FragmentActivity {
  import FragmentLayoutSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_layout_support)
  }

}

object FragmentLayoutSupport {

  /**
   * This is a secondary activity, to show what the user has selected
   * when the screen is not large enough to show it all in one activity.
   */

  private class DetailsActivity extends FragmentActivity {

    override protected def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)

      if (getResources.getConfiguration.orientation
                    == Configuration.ORIENTATION_LANDSCAPE) {
        // If the screen is now in landscape mode, we can show the
        // dialog in-line with the list so we don't need this activity.
        finish()
        return
      }

      if (savedInstanceState == null) {
        // During initial setup, plug in the details fragment.
        val details = new DetailsFragment()
        details.setArguments(getIntent.getExtras)
        getSupportFragmentManager.beginTransaction().add(
                        android.R.id.content, details).commit()
      }
    }
  }

  /**
   * This is the "top-level" fragment, showing a list of items that the
   * user can pick.  Upon picking an item, it takes care of displaying the
   * data to the user as appropriate based on the currrent UI layout.
   */
  private class TitlesFragment extends ListFragment {
    private var mDualPane: Boolean = _
    private var mCurCheckPosition = 0

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      // Populate list with our static array of titles.
      setListAdapter(new ArrayAdapter[String](getActivity,
                    R.layout.simple_list_item_checkable_1,
                    android.R.id.text1, Shakespeare.TITLES))

      // Check to see if we have a frame in which to embed the details
      // fragment directly in the containing UI.
      val detailsFrame = getActivity.findViewById(R.id.details)
      mDualPane = detailsFrame != null && detailsFrame.getVisibility == View.VISIBLE

      if (savedInstanceState != null) {
        // Restore last state for checked position.
        mCurCheckPosition = savedInstanceState.getInt("curChoice", 0)
      }

      if (mDualPane) {
        // In dual-pane mode, the list view highlights the selected item.
        getListView setChoiceMode ListView.CHOICE_MODE_SINGLE
        // Make sure our UI is in the correct state.
        showDetails(mCurCheckPosition)
      }
    }

    override def onSaveInstanceState(outState: Bundle) {
      super.onSaveInstanceState(outState)
      outState.putInt("curChoice", mCurCheckPosition)
    }

    override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
      showDetails(position)
    }

    /**
     * Helper function to show the details of a selected item, either by
     * displaying a fragment in-place in the current UI, or starting a
     * whole new activity in which it is displayed.
     */
    private def showDetails(index: Int) {
      mCurCheckPosition = index

      if (mDualPane) {
        // We can display everything in-place with fragments, so update
        // the list to highlight the selected item and show the data.
        getListView.setItemChecked(index, true)

        // Check what fragment is currently shown, replace if needed.
        var details = getFragmentManager.findFragmentById(R.id.details).asInstanceOf[DetailsFragment]
        if (details == null || details.getShownIndex != index) {
          // Make new fragment to show this selection.
          details = DetailsFragment.newInstance(index)

          // Execute a transaction, replacing any existing fragment
          // with this one inside the frame.
          val ft = getFragmentManager().beginTransaction()
          ft.replace(R.id.details, details)
          ft setTransition FragmentTransaction.TRANSIT_FRAGMENT_FADE
          ft.commit()
        }

      } else {
        // Otherwise we need to launch a new activity to display
        // the dialog fragment with selected text.
        val intent = new Intent()
        intent.setClass(getActivity, classOf[DetailsActivity])
        intent.putExtra("index", index)
        startActivity(intent)
      }
    }
  }

  private object DetailsFragment {
    /**
     * Create a new instance of DetailsFragment, initialized to
     * show the text at 'index'.
     */
    def newInstance(index: Int): DetailsFragment = {
      val f = new DetailsFragment()

      // Supply index input as an argument.
      val args = new Bundle()
      args.putInt("index", index)
      f setArguments args

      f
    }
  }

  /**
   * This is the secondary fragment, displaying the details of a particular
   * item.
   */
  private class DetailsFragment extends Fragment {

    def getShownIndex: Int = getArguments.getInt("index", 0)

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      if (container == null) {
        // We have different layouts, and in one of them this
        // fragment's containing frame doesn't exist.  The fragment
        // may still be created from its saved state, but there is
        // no reason to try to create its view hierarchy because it
        // won't be displayed.  Note this is not needed -- we could
        // just run the code below, where we would create and return
        // the view hierarchy; it would just never be used.
        null
      }
      else {
        val scroller = new ScrollView(getActivity)
        val text = new TextView(getActivity)
        val padding = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    4, getActivity.getResources.getDisplayMetrics).toInt
        text.setPadding(padding, padding, padding, padding)
        scroller addView text
        text setText Shakespeare.DIALOGUE(getShownIndex)
        scroller
      }
    }
  }

}
