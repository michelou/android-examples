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

package com.example.android.supportv4

import android.app.ListActivity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.{ListView, SimpleAdapter}

import java.util.{Map => JMap}
import scala.collection.JavaConversions._
import scala.collection.mutable.{ListBuffer, HashSet}

class Support4Demos extends ListActivity {

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
        
    val intent = getIntent
    var path = intent getStringExtra "com.example.android.apis.Path"
        
    if (path == null) path = ""

    setListAdapter(new SimpleAdapter(this, getData(path),
                   android.R.layout.simple_list_item_1, Array("title"),
                   Array(android.R.id.text1)))
    getListView setTextFilterEnabled true
  }

  protected def getData(prefix: String): List[JMap[String, AnyRef]] = {
    val myData = new ListBuffer[JMap[String, AnyRef]]()

    val mainIntent = new Intent(Intent.ACTION_MAIN, null)
    mainIntent addCategory "com.example.android.supportv4.SUPPORT4_SAMPLE_CODE"

    val pm = getPackageManager
    val list = pm.queryIntentActivities(mainIntent, 0)

    if (null == list) return List()

    val (prefixPath, prefixWithSlash) =
      if (prefix equals "") (null, prefix)
      else (prefix split "/",  prefix + "/")

    val entries = new HashSet[String]()

    for (info <- list) {
      val labelSeq = info loadLabel pm
      val label =
        if (labelSeq != null) labelSeq.toString
        else info.activityInfo.name

      if (prefixWithSlash.length == 0 || label.startsWith(prefixWithSlash)) {
                
        val labelPath = label split "/"
        val nextLabel =
          if (prefixPath == null) labelPath(0)
          else labelPath(prefixPath.length)

        val n = if (prefixPath != null) prefixPath.length else 0
        if (n == labelPath.length - 1)
          addItem(myData, nextLabel, activityIntent(
                  info.activityInfo.applicationInfo.packageName,
                  info.activityInfo.name))
        else if (!entries.contains(nextLabel)) {
          val path = if (prefix equals "") nextLabel else prefix + "/" + nextLabel
          addItem(myData, nextLabel, browseIntent(path))
          entries += nextLabel
        }
      }
    }
    myData.sortWith(_("title").asInstanceOf[String] < _("title").asInstanceOf[String]).toList
  }

  protected def activityIntent(pkg: String, componentName: String): Intent = {
    val result = new Intent()
    result.setClassName(pkg, componentName)
    result
  }
    
  protected def browseIntent(path: String): Intent = {
    val result = new Intent()
    result.setClass(this, classOf[Support4Demos])
    result.putExtra("com.example.android.apis.Path", path)
    result
  }

  protected def addItem(data: ListBuffer[JMap[String, AnyRef]], name: String, intent: Intent) {
    data add Map("title" -> name, "intent" -> intent)
  }

  @SuppressWarnings(Array("unchecked"))
  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    val map = l.getItemAtPosition(position).asInstanceOf[JMap[String, AnyRef]]

    val intent = map.get("intent").asInstanceOf[Intent]
    startActivity(intent)
  }
}
