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

import com.example.android.supportv4.R

import java.io.File
import java.text.Collator
import java.util.{List => JList}

import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.content.pm.{ActivityInfo, ApplicationInfo, PackageManager}
import android.content.res.{Configuration, Resources}
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.app.{FragmentActivity, FragmentManager, ListFragment,
                               LoaderManager}
import android.support.v4.content.{AsyncTaskLoader, Loader}
import android.text.TextUtils
import android.util.Log
import android.view.{LayoutInflater, Menu, MenuInflater, MenuItem, View, ViewGroup}
import android.widget.{ArrayAdapter, ImageView, ListView, TextView}

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

/**
 * Demonstration of the implementation of a custom Loader.
 */
class LoaderCustomSupport extends FragmentActivity {
  import LoaderCustomSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    val fm = getSupportFragmentManager

    // Create the list fragment and add it as our sole content.
    if (fm.findFragmentById(android.R.id.content) == null) {
      val list = new AppListFragment()
      fm.beginTransaction().add(android.R.id.content, list).commit()
    }
  }

}

object LoaderCustomSupport {

  /**
   * This class holds the per-item data in our Loader.
   */
  private[app] class AppEntry(loader: AppListLoader, info: ApplicationInfo) {
    private val mApkFile = new File(info.sourceDir)
    private var mLabel: String = _
    private var mMounted: Boolean = _
    private var mIcon: Drawable = _

    def getApplicationInfo: ApplicationInfo = info

    def getLabel: String = mLabel

    def getIcon(): Drawable = {
      if (mIcon == null) {
        if (mApkFile.exists) {
          mIcon = info loadIcon loader.mPm
          mIcon
        } else {
          mMounted = false
          loader.getContext.getResources.getDrawable(
                    android.R.drawable.sym_def_app_icon)
        }
      } else if (!mMounted) {
        // If the app wasn't mounted but is now mounted, reload its icon.
        if (mApkFile.exists) {
          mMounted = true
          mIcon = info loadIcon loader.mPm
          mIcon
        } else
          loader.getContext.getResources.getDrawable(
                    android.R.drawable.sym_def_app_icon)
      } else
        mIcon
    }

    override def toString: String = mLabel

    def loadLabel(context: Context) {
      if (mLabel == null || !mMounted) {
        if (!mApkFile.exists) {
          mMounted = false
          mLabel = info.packageName
        } else {
          mMounted = true
          val label = info loadLabel context.getPackageManager
          mLabel = if (label != null) label.toString else info.packageName
        }
      }
    }

  }

  /**
   * Helper for determining if the configuration has changed in an interesting
   * way so we need to rebuild the app list.
   */
  private class InterestingConfigChanges {
    private val mLastConfiguration = new Configuration()
    private var mLastDensity: Int = _

    def applyNewConfig(res: Resources): Boolean = {
      val configChanges = mLastConfiguration.updateFrom(res.getConfiguration)
      val densityChanged = mLastDensity != res.getDisplayMetrics.densityDpi
      if (densityChanged || (configChanges&(ActivityInfo.CONFIG_LOCALE
                    |ActivityInfo.CONFIG_UI_MODE|ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
        mLastDensity = res.getDisplayMetrics().densityDpi
        true
      }
      else
        false
    }
  }

  /**
   * Helper class to look for interesting changes to the installed apps
   * so that the loader can be updated.
   */
  private class PackageIntentReceiver(loader: AppListLoader) extends BroadcastReceiver {
    {
      val filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED)
      filter addAction Intent.ACTION_PACKAGE_REMOVED
      filter addAction Intent.ACTION_PACKAGE_CHANGED
      filter addDataScheme "package"
      loader.getContext.registerReceiver(this, filter)
      // Register for events related to sdcard installation.
      val sdFilter = new IntentFilter()
      sdFilter addAction Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE
      sdFilter addAction Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE
      loader.getContext.registerReceiver(this, sdFilter)
    }

    override def onReceive(context: Context, intent: Intent) {
      // Tell the loader about the change.
      loader.onContentChanged()
    }
  }

  /**
   * A custom Loader that loads all of the installed applications.
   */
  private class AppListLoader(context: Context)
  extends AsyncTaskLoader[JList[AppEntry]](context) {
    private val mLastConfig = new InterestingConfigChanges()
    // Retrieve the package manager for later use; note we don't
    // use 'context' directly but instead the save global application
    // context returned by getContext().
    val mPm = getContext.getPackageManager

    private var mApps: JList[AppEntry] = _
    private var mPackageObserver: PackageIntentReceiver = _

    /**
     * This is where the bulk of our work is done.  This function is
     * called in a background thread and should generate a new set of
     * data to be published by the loader.
     */
    override def loadInBackground(): JList[AppEntry] = {
      // Retrieve all known applications.
      val apps: List[ApplicationInfo] = {
        val jApps = mPm.getInstalledApplications(
                      PackageManager.GET_UNINSTALLED_PACKAGES |
                      PackageManager.GET_DISABLED_COMPONENTS)
        if (jApps == null) List() else jApps.toList
      }
      val context = getContext()

      // Create corresponding array of entries and load their labels.
      val entries = for (app <- apps) yield {
        val entry = new AppEntry(this, app)
        entry loadLabel context
        entry
      }

      entries.sortWith(_.getLabel < _.getLabel)
    }

    /**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    override def deliverResult(apps: JList[AppEntry]) {
      if (isReset) {
        // An async query came in while the loader is stopped.  We
        // don't need the result.
        if (apps != null) onReleaseResources(apps)
      }
      val oldApps = apps
      mApps = apps

      if (isStarted()) {
        // If the Loader is currently started, we can immediately
        // deliver its results.
        super.deliverResult(apps)
      }

      // At this point we can release the resources associated with
      // 'oldApps' if needed; now that the new result is delivered we
      // know that it is no longer in use.
      if (oldApps != null) onReleaseResources(oldApps)
    }

    /**
     * Handles a request to start the Loader.
     */
    override protected def onStartLoading() {
      if (mApps != null) {
        // If we currently have a result available, deliver it immediately.
        deliverResult(mApps)
      }

      // Start watching for changes in the app data.
      if (mPackageObserver == null) {
        mPackageObserver = new PackageIntentReceiver(this)
      }

      // Has something interesting in the configuration changed since we
      // last built the app list?
      val configChange = mLastConfig applyNewConfig getContext.getResources

      if (takeContentChanged() || mApps == null || configChange) {
        // If the data has changed since the last time it was loaded
        // or is not currently available, start a load.
        forceLoad()
      }
    }

    /**
     * Handles a request to stop the Loader.
     */
    override protected def onStopLoading() {
      // Attempt to cancel the current load task if possible.
      cancelLoad()
    }

    /**
     * Handles a request to cancel a load.
     */
    override def onCanceled(apps: JList[AppEntry]) {
      super.onCanceled(apps)

      // At this point we can release the resources associated with 'apps'
      // if needed.
      onReleaseResources(apps)
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    override protected def onReset() {
      super.onReset()

      // Ensure the loader is stopped
      onStopLoading()

      // At this point we can release the resources associated with 'apps'
      // if needed.
      if (mApps != null) {
        onReleaseResources(mApps)
         mApps = null
      }

      // Stop monitoring for changes.
      if (mPackageObserver != null) {
        getContext unregisterReceiver mPackageObserver
        mPackageObserver = null
      }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected def onReleaseResources(apps: JList[AppEntry]) {
      // For a simple List<> there is nothing to do.  For something
      // like a Cursor, we would close it here.
    }
  }

  private class AppListAdapter(context: Context)
  extends ArrayAdapter[AppEntry](context, android.R.layout.simple_list_item_2) {
    private val mInflater =
      context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    def setData(data: JList[AppEntry]) {
      clear()
      if (data != null) {
        //addAll(data); //API 11
        val it = data.iterator(); while (it.hasNext())
        add(it.next())
      }
    }

    /**
     * Populate new items in the list.
     */
    override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val view =
        if (convertView == null)
          mInflater.inflate(R.layout.list_item_icon_text, parent, false)
        else
          convertView

      val item: AppEntry = getItem(position)
      view.findViewById(R.id.icon).asInstanceOf[ImageView] setImageDrawable item.getIcon
      view.findViewById(R.id.text).asInstanceOf[TextView] setText item.getLabel

      view
    }
  }

  private[app] class AppListFragment extends ListFragment
            with /*OnQueryTextListener,*/ LoaderManager.LoaderCallbacks[JList[AppEntry]] {

    // This is the Adapter being used to display the list's data.
    private var mListAdapter: AppListAdapter = _

    // If non-null, this is the current filter the user has provided.
    private var mCurFilter: String = _

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      // Give some text to display if there is no data.  In a real
      // application this would come from a resource.
      setEmptyText("No applications")

      // We have a menu item to show in action bar.
      setHasOptionsMenu(true)

      // Create an empty adapter we will use to display the loaded data.
      mListAdapter = new AppListAdapter(getActivity)
      setListAdapter(mListAdapter)

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
/*
        @Override public boolean onQueryTextChange(String newText) {
            // Called when the action bar search text has changed.  Since this
            // is a simple array adapter, we can just have it do the filtering.
            mCurFilter = !TextUtils.isEmpty(newText) ? newText : null;
            mListAdapter.getFilter().filter(mCurFilter);
            return true;
        }

        @Override public boolean onQueryTextSubmit(String query) {
            // Don't care about this.
            return true;
        }
*/
    override def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
      // Insert desired behavior here.
      Log.i("LoaderCustom", "Item clicked: " + id)
    }

    override def onCreateLoader(id: Int, args: Bundle): Loader[JList[AppEntry]] = {
      // This is called when a new Loader needs to be created.  This
      // sample only has one Loader with no arguments, so it is simple.
      new AppListLoader(getActivity)
    }

    override def onLoadFinished(loader: Loader[JList[AppEntry]], data: JList[AppEntry]) {
      // Set the new data in the adapter.
      mListAdapter setData data

      // The list should now be shown.
      if (isResumed) setListShown(true)
      else setListShownNoAnimation(true)
    }

    override def onLoaderReset(loader: Loader[JList[AppEntry]]) {
      // Clear the data in the adapter.
      mListAdapter setData null
    }
  }

}
