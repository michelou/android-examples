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

import android.app.ListActivity
import android.content.Context
import android.os.Bundle
import android.content.res.ColorStateList
import android.view.{View, ViewGroup}
import android.widget.{AdapterView, ArrayAdapter, ListView, TextView}

class GradientDemo extends ListActivity {
  import GradientDemo._  // companion object
    
  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)
    setListAdapter(new GradientAdapter(this))
    getListView setOnItemSelectedListener listener
  }

  private class GradientAdapter(context: Context)
  extends ArrayAdapter(context, R.layout.row, items) {

     override def getView(position: Int, convertView0: View,
                          parent: ViewGroup): View = {
       val (convertView, wrapper) = if (convertView0 == null) {
         val cv = getLayoutInflater.inflate(R.layout.row, parent, false)
         val wrapper = new GradientWrapper(cv)
         cv setTag wrapper
         (cv, wrapper)
       }
       else {
         val wrapper = convertView0.getTag.asInstanceOf[GradientWrapper]
         (convertView0, wrapper)
       }

       wrapper.label setText items(position)

       convertView
     }
  }

  private class GradientWrapper(row: View) {
    val label = row.findViewById(R.id.label).asInstanceOf[TextView]
  }

  private val listener = new AdapterView.OnItemSelectedListener() {
    var lastRow: View = null

    def onItemSelected(parent: AdapterView[_], view: View,
                       position: Int, id: Long) {
      if (lastRow != null) {
        lastRow setBackgroundColor 0x00000000
      }
            
      view setBackgroundResource R.drawable.active_row
      lastRow = view
    }

    def onNothingSelected(parent: AdapterView[_]) {
      if (lastRow != null) {
        lastRow setBackgroundColor 0x00000000
        lastRow = null
      }
    }
  }
}
object GradientDemo {
  private val allWhite = ColorStateList.valueOf(0xFFFFFFFF)
  private val items = Array(
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetuer",
    "adipiscing", "elit", "morbi", "vel", "ligula", "vitae",
    "arcu", "aliquet", "mollis", "etiam", "vel", "erat", "placerat",
    "ante", "porttitor", "sodales", "pellentesque", "augue", "purus"
  )
}
