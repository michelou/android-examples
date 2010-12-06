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

import android.app.SearchManager
import android.content.Intent
import android.widget.{ArrayAdapter, ListAdapter}

import scala.collection.mutable.ListBuffer

class LoremSearch extends LoremBase {

  override def makeMeAnAdapter(intent: Intent): ListAdapter = {
    var adapter: ListAdapter = null

    if (intent.getAction equals Intent.ACTION_SEARCH) {
      val query = intent getStringExtra SearchManager.QUERY
      val results = searchItems(query)

      import scala.collection.JavaConversions._
      adapter = new ArrayAdapter[String](this,
        android.R.layout.simple_list_item_1, results)
      setTitle("LoremSearch for: "+query)
    }

    adapter
  }

  private def searchItems(query: String): List[String] = {
    LoremSuggestionProvider.getBridge(this).saveRecentQuery(query, null)

    items filter (_.indexOf(query) > -1)
  }
}
