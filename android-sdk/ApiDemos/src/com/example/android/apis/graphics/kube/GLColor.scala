/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.example.android.apis.graphics.kube

class GLColor(val red: Int, val green: Int, val blue: Int, val alpha: Int) {

  def this(red: Int, green: Int, blue: Int) =
    this(red, green, blue, 0x10000)

  override def equals(other: Any): Boolean =
    other match {
      case color: GLColor =>
        red == color.red && green == color.green &&
        blue == color.blue && alpha == color.alpha
      case _ =>
        false
    }
}
