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

package com.example.android.supportv4.view

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.widget.{Checkable, FrameLayout}

class CheckableFrameLayout(context: Context, attrs: AttributeSet)
extends FrameLayout(context, attrs) with Checkable {
  import CheckableFrameLayout._

  private var mChecked: Boolean = _

  def this(context: Context) = this(context, null)

  def setChecked(checked: Boolean) {
    mChecked = checked
    setBackgroundDrawable(if (checked) COLOR_CHECKED else null)
  }

  def isChecked: Boolean = mChecked

  def toggle() { setChecked(!mChecked) }

}

object CheckableFrameLayout {
  private val COLOR_CHECKED = new ColorDrawable(0xff0000a0)
}

