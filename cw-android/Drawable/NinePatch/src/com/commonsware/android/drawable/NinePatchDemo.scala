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

package com.commonsware.android.drawable

import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{LinearLayout, SeekBar}

import scala.android.app.Activity

class NinePatchDemo extends Activity {
  private var horizontal: SeekBar = _
  private var vertical: SeekBar = _
  private var thingToResize: View = _
    
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    thingToResize = findViewById(R.id.resize)

    horizontal= findView(R.id.horizontal)
    vertical = findView(R.id.vertical)
        
    horizontal setMax 176    // 240 less 64 starting size
    vertical setMax 176        // keep it square @ max

    horizontal setOnSeekBarChangeListener h
    vertical setOnSeekBarChangeListener v
  }

  private val h = new SeekBar.OnSeekBarChangeListener() {
    def onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
      val old = thingToResize.getLayoutParams
      val current = new LinearLayout.LayoutParams(64+progress, old.height)

      thingToResize setLayoutParams current
    }

    def onStartTrackingTouch(seekBar: SeekBar) {
      // unused
    }

    def onStopTrackingTouch(seekBar: SeekBar) {
      // unused
    }
  }

  private val v = new SeekBar.OnSeekBarChangeListener() {
    def onProgressChanged(seekBar: SeekBar, progress: Int, fromTouch: Boolean) {
      val old = thingToResize.getLayoutParams
      val current = new LinearLayout.LayoutParams(old.width, 64+progress)
      thingToResize setLayoutParams current
    }

    def onStartTrackingTouch(seekBar: SeekBar) {
      // unused
    }

    def onStopTrackingTouch(seekBar: SeekBar) {
      // unused
    }
  }
}
