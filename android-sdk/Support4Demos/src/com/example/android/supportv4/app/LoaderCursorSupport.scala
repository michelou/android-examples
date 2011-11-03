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

import android.support.v4.app.{FragmentActivity, FragmentManager, ListFragment, LoaderManager}
import android.support.v4.content.{CursorLoader, Loader}
import android.support.v4.widget.SimpleCursorAdapter

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.{Menu, MenuInflater, View}
import android.widget.ListView

import scala.android.provider.ContactsContract.Contacts

/**
 * Demonstration of the use of a CursorLoader to load and display contacts
 * data in a fragment.
 */
class LoaderCursorSupport extends FragmentActivity {
  import LoaderCursorSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val fm = getSupportFragmentManager

    // Create the list fragment and add it as our sole content.
    if (fm.findFragmentById(android.R.id.content) == null) {
      val list = new CursorLoaderListFragment()
      fm.beginTransaction().add(android.R.id.content, list).commit()
    }
  }

}

object LoaderCursorSupport {

  private object CursorLoaderListFragment {
    // These are the Contacts rows that we will retrieve.
    final val CONTACTS_SUMMARY_PROJECTION = Array(
      Contacts._ID,
      Contacts.DISPLAY_NAME,
      Contacts.CONTACT_STATUS,
      Contacts.CONTACT_PRESENCE,
      Contacts.PHOTO_ID,
      Contacts.LOOKUP_KEY
    )
  }

  private[app] class CursorLoaderListFragment extends ListFragment
            with LoaderManager.LoaderCallbacks[Cursor] {
    import CursorLoaderListFragment._  // companion object

    // This is the Adapter being used to display the list's data.
    private var mCursorAdapter: SimpleCursorAdapter = _

    // If non-null, this is the current filter the user has provided.
    private var mCurFilter: String = _

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      // Give some text to display if there is no data.  In a real
      // application this would come from a resource.
      setEmptyText("No phone numbers")

      // We have a menu item to show in action bar.
      setHasOptionsMenu(true)

     // Create an empty adapter we will use to display the loaded data.
      mCursorAdapter = new SimpleCursorAdapter(getActivity,
                    android.R.layout.simple_list_item_2, null,
                    Array(Contacts.DISPLAY_NAME, Contacts.CONTACT_STATUS),
                    Array(android.R.id.text1, android.R.id.text2), 0)
      setListAdapter(mCursorAdapter)

      // Start out with a progress indicator.
      setListShown(false)

      // Prepare the loader.  Either re-connect with an existing one,
      // or start a new one.
      getLoaderManager.initLoader(0, null, this)
    }

    override def onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
      // Place an action bar item for searching.
      //MenuItem item = menu.add("Search");
      //item.setIcon(android.R.drawable.ic_menu_search);
      //item.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      //SearchView sv = new SearchView(getActivity());
      //sv.setOnQueryTextListener(this);
      //item.setActionView(sv);
    }

    def onQueryTextChange(newText: String): Boolean = {
      // Called when the action bar search text has changed.  Update
      // the search filter, and restart the loader to do a new query
      // with this filter.
      mCurFilter = if (!TextUtils.isEmpty(newText)) newText else null
      getLoaderManager.restartLoader(0, null, this)
      true
    }

    override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
      // Insert desired behavior here.
      Log.i("FragmentComplexList", "Item clicked: " + id)
    }

    def onCreateLoader(id: Int, args: Bundle): Loader[Cursor] = {
      // This is called when a new Loader needs to be created.  This
      // sample only has one Loader, so we don't care about the ID.
      // First, pick the base URI to use depending on whether we are
      // currently filtering.
      val baseUri =
        if (mCurFilter != null)
          Uri.withAppendedPath(Contacts.CONTENT_FILTER_URI,
                        Uri.encode(mCurFilter))
        else
          Contacts.CONTENT_URI

      // Now create and return a CursorLoader that will take care of
      // creating a Cursor for the data being displayed.
      val select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND (" +
                   Contacts.HAS_PHONE_NUMBER + "=1) AND (" +
                   Contacts.DISPLAY_NAME + " != '' ))"
      new CursorLoader(getActivity, baseUri,
                    CONTACTS_SUMMARY_PROJECTION, select, null,
                    Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC")
    }

    def onLoadFinished(loader: Loader[Cursor], data: Cursor) {
      // Swap the new cursor in.  (The framework will take care of closing the
      // old cursor once we return.)
      mCursorAdapter swapCursor data

      // The list should now be shown.
      if (isResumed) setListShown(true)
      else setListShownNoAnimation(true)
    }

    def onLoaderReset(loader: Loader[Cursor]) {
      // This is called when the last Cursor provided to onLoadFinished()
      // above is about to be closed.  We need to make sure we are no
      // longer using it.
      mCursorAdapter swapCursor null
    }
  }

}
