/***
    Copyright (c) 2008-2010 CommonsWare, LLC
    
    Licensed under the Apache License, Version 2.0 (the "License"); you may
    not use this file except in compliance with the License. You may obtain
    a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/

package com.commonsware.android.search

import android.app.Activity._
import android.app.{ListActivity, SearchManager}
import android.content.Intent
import android.os.Bundle
import android.view.{Menu, MenuItem, View}
import android.widget.{ListAdapter, ListView, TextView, Toast}
import org.xmlpull.v1.{XmlPullParser, XmlPullParserException}

import scala.collection.mutable.ListBuffer

abstract class LoremBase extends ListActivity {
  import LoremBase._  // companion object

  def makeMeAnAdapter(intent: Intent): ListAdapter
  private var selection: TextView = _
  protected var items: List[String] = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)
    selection = findViewById(R.id.selection).asInstanceOf[TextView]

    try {
      val xpp = getResources getXml R.xml.words
      val buf = new ListBuffer[String]()

      while (xpp.getEventType != XmlPullParser.END_DOCUMENT) {
        if (xpp.getEventType == XmlPullParser.START_TAG) {
          if (xpp.getName equals "word") {
            buf += xpp getAttributeValue 0
          }
        }

        xpp.next()
      }
      items = buf.toList
    }
    catch {
      case t: Throwable =>
        Toast.makeText(this, "Request failed: "+t.toString, 4000).show()
    }

    setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL)

    onNewIntent(getIntent)
  }

  override def onNewIntent(intent: Intent) {
    val adapter = makeMeAnAdapter(intent)

    if (adapter == null) finish()
    else setListAdapter(adapter)
  }

  override def onListItemClick(parent: ListView, v: View,
                               position: Int, id: Long) {
    selection setText items(position).toString
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(Menu.NONE, LOCAL_SEARCH_ID, Menu.NONE, "Local Search")
        .setIcon(android.R.drawable.ic_search_category_default)
    menu.add(Menu.NONE, GLOBAL_SEARCH_ID, Menu.NONE, "Global Search")
        .setIcon(R.drawable.search)
        .setAlphabeticShortcut(SearchManager.MENU_KEY)
    
    super.onCreateOptionsMenu(menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case LOCAL_SEARCH_ID =>
        onSearchRequested()
        true
      case GLOBAL_SEARCH_ID =>
        startSearch(null, false, null, true)
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }
}

object LoremBase {
  private final val LOCAL_SEARCH_ID = Menu.FIRST+1
  private final val GLOBAL_SEARCH_ID = Menu.FIRST+2
}
