/*
 * Copyright (C) 2008 Google Inc.
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

package com.example.anycut

import android.app.{Activity, ListActivity}
import android.content.{ComponentName, Intent}
import android.content.pm.{PackageManager, ResolveInfo}
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.os.{AsyncTask, Bundle}
import android.view.{LayoutInflater, View, ViewGroup, Window}
import android.widget.{ArrayAdapter, ListView, TextView}

import java.util.{Collections => JCollections, List => JList}

import scala.collection.JavaConversions._

/**
 * Presents a list of activities to choose from. This list only contains activities
 * that have ACTION_MAIN, since other types may require data as input.
 */
class ActivityPickerActivity extends ListActivity {
  private var mPackageManager: PackageManager = _

  /**
   * This class is used to wrap ResolveInfo so that it can be filtered using
   * ArrayAdapter's built int filtering logic, which depends on toString().
   */
  private case class ResolveInfoWrapper(info: ResolveInfo) {
    override def toString: String = (info loadLabel mPackageManager).toString
  }

  private class ActivityAdapter(activity: Activity, activities: List[ResolveInfoWrapper])
  extends ArrayAdapter[ResolveInfoWrapper](activity, 0, activities.toArray) {
    private val mInflater: LayoutInflater = activity.getLayoutInflater

    override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val infoWrapper: ResolveInfoWrapper = getItem(position)

      var view = convertView
      if (view == null) {
        // Inflate the view and cache the pointer to the text view
        view = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
        view setTag (view findViewById android.R.id.text1)
      }

      val textView = view.getTag.asInstanceOf[TextView]
      textView setText (infoWrapper.info loadLabel mPackageManager)

      view
    }
  }

  private final class LoadingTask extends AsyncTask[Object, Object, ActivityAdapter] {
    override def onPreExecute() {
      setProgressBarIndeterminateVisibility(true)
    }

    override def doInBackground(params: AnyRef*): ActivityAdapter = {
      // Load the activities
      val queryIntent = new Intent(Intent.ACTION_MAIN)
      val list: JList[ResolveInfo] = mPackageManager.queryIntentActivities(queryIntent, 0)

      // Sort the list
      JCollections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager))

      // Make the wrappers
      val activities = list.toList map (item => ResolveInfoWrapper(item))
      new ActivityAdapter(ActivityPickerActivity.this, activities)
    }

    override def onPostExecute(result: ActivityAdapter) {
      setProgressBarIndeterminateVisibility(false)
      setListAdapter(result)
    }
  }

  override protected def onCreate(savedState: Bundle) {
    super.onCreate(savedState)

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    setContentView(R.layout.list)

    getListView setTextFilterEnabled true

    mPackageManager = getPackageManager

    // Start loading the data
    new LoadingTask().execute(null.asInstanceOf[Array[AnyRef]])
  }

  override protected def onListItemClick(list: ListView, view: View, position: Int, id: Long) {
    val wrapper = (getListAdapter getItem position).asInstanceOf[ResolveInfoWrapper]
    val info = wrapper.info

    // Build the intent for the chosen activity
    val intent = new Intent()
    intent setComponent new ComponentName(info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name)
    val result = new Intent()
    result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent)

    // Set the name of the activity
    result.putExtra(Intent.EXTRA_SHORTCUT_NAME, info loadLabel mPackageManager)

    // Build the icon info for the activity
    val drawable = info loadIcon mPackageManager
    drawable match {
      case bd: BitmapDrawable =>
        result.putExtra(Intent.EXTRA_SHORTCUT_ICON, bd.getBitmap)
      case _ =>
    }
//  val iconResource = new ShortcutIconResource()
//  iconResource.packageName = info.activityInfo.packageName
//  iconResource.resourceName = getResources getResourceEntryName info.getIconResource
//  result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource)

    // Set the result
    setResult(Activity.RESULT_OK, result)
    finish()
  }
}
