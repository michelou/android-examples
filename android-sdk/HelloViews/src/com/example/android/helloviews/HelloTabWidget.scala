/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.example.android.helloviews

import android.app.TabActivity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.TabHost

class HelloTabWidget extends TabActivity {

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.tabwidget)

    val res = getResources            // Resource object to get Drawables
    val tabHost = getTabHost          // The activity TabHost
    var spec: TabHost#TabSpec = null  // Resusable TabSpec for each tab
    var intent: Intent = null         // Reusable Intent for each tab

    // Create an Intent to launch an Activity for the tab (to be reused)
    intent = new Intent().setClass(this, classOf[ArtistsActivity])

    // Initialize a TabSpec for each tab and add it to the TabHost
    spec = tabHost.newTabSpec("artists").setIndicator("Artists",
                          res.getDrawable(R.drawable.ic_tab_artists))
                      .setContent(intent)
    tabHost addTab spec

    // Do the same for the other tabs
    intent = new Intent().setClass(this, classOf[AlbumsActivity])
    spec = tabHost.newTabSpec("albums")
                  .setIndicator("Albums", res getDrawable R.drawable.ic_tab_albums)
                  .setContent(intent)
    tabHost addTab spec

    intent = new Intent().setClass(this, classOf[SongsActivity])
    spec = tabHost.newTabSpec("songs")
                  .setIndicator("Songs", res getDrawable R.drawable.ic_tab_songs)
                  .setContent(intent)
    tabHost addTab spec

    tabHost setCurrentTab 2
  }
}
