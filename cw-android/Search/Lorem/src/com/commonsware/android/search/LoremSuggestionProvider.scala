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

import android.content.{Context, SearchRecentSuggestionsProvider}
import android.content.SearchRecentSuggestionsProvider._
import android.provider.SearchRecentSuggestions

class LoremSuggestionProvider extends SearchRecentSuggestionsProvider {
  import LoremSuggestionProvider._  // companion object

  setupSuggestions(AUTH, DATABASE_MODE_QUERIES)
}

object LoremSuggestionProvider {
  private val AUTH =
    "com.commonsware.android.search.LoremSuggestionProvider"

  def getBridge(context: Context): SearchRecentSuggestions =
    new SearchRecentSuggestions(context, AUTH, DATABASE_MODE_QUERIES)

}
