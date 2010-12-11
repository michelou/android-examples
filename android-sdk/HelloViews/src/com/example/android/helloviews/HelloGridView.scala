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

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.{View, ViewGroup}
import android.widget.{AdapterView, BaseAdapter, GridView, ImageView, TextView, Toast}

class HelloGridView extends Activity {
  import HelloGridView._  // companion object

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.gridview)

    val gridview = findViewById(R.id.gridview).asInstanceOf[GridView]
    gridview setAdapter new ImageAdapter(this)

    gridview setOnItemClickListener new AdapterView.OnItemClickListener() {
      def onItemClick(parent: AdapterView[_], v: View, position: Int, id: Long) {
        Toast.makeText(HelloGridView.this, "" + position, Toast.LENGTH_SHORT).show()
      }
    }
  }

}

object HelloGridView {

  class ImageAdapter(context: Context) extends BaseAdapter {

    def getCount: Int = mThumbIds.length

    def getItem(position: Int): AnyRef = null

    def getItemId(position: Int): Long = 0

    // create a new ImageView for each item referenced by the Adapter
    def getView(position: Int, convertView: View, parent: ViewGroup): View = {
      val imageView: ImageView =
        if (convertView == null) {  // if it's not recycled, initialize some attributes
          val view = new ImageView(context)
          view setAdjustViewBounds true
          view setLayoutParams new ViewGroup.LayoutParams(85, 85)
          view setScaleType ImageView.ScaleType.CENTER_CROP
          view.setPadding(8, 8, 8, 8)
          view
        } else {
          convertView.asInstanceOf[ImageView]
        }

      imageView setImageResource mThumbIds(position)
      imageView
    }

    // references to our images
    private val mThumbIds = Array(
      R.drawable.sample_2, R.drawable.sample_3,
      R.drawable.sample_4, R.drawable.sample_5,
      R.drawable.sample_6, R.drawable.sample_7,
      R.drawable.sample_0, R.drawable.sample_1,
      R.drawable.sample_2, R.drawable.sample_3,
      R.drawable.sample_4, R.drawable.sample_5,
      R.drawable.sample_6, R.drawable.sample_7,
      R.drawable.sample_0, R.drawable.sample_1,
      R.drawable.sample_2, R.drawable.sample_3,
      R.drawable.sample_4, R.drawable.sample_5,
      R.drawable.sample_6, R.drawable.sample_7
    )
  }
}
