/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.example.android.multires

import scala.android.app.Activity

import android.os.Bundle
import android.view.View
import android.widget.{Button, ImageView, TextView}

final class MultiRes extends Activity {
  import MultiRes._  // companion object

  private var mCurrentPhotoIndex = 0
  private def incrAndGetIndex(): Int = {
    mCurrentPhotoIndex = (mCurrentPhotoIndex + 1) % mPhotoCount
    mCurrentPhotoIndex
  }

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    showPhoto(mCurrentPhotoIndex)

    // Handle clicks on the 'Next' button.
    val nextButton: Button = findView(R.id.next_button)
    nextButton setOnClickListener {
      //mCurrentPhotoIndex = (mCurrentPhotoIndex + 1) % mPhotoCount
      showPhoto(incrAndGetIndex()) //mCurrentPhotoIndex)
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    outState.putInt("photo_index", mCurrentPhotoIndex)
    super.onSaveInstanceState(outState)
  }

  override protected def onRestoreInstanceState(savedInstanceState: Bundle) {
    mCurrentPhotoIndex = savedInstanceState getInt "photo_index"
    showPhoto(mCurrentPhotoIndex)
    super.onRestoreInstanceState(savedInstanceState)
  }

  private def showPhoto(photoIndex: Int) {
    val imageView: ImageView = findView(R.id.image_view)
    imageView setImageResource mPhotoIds(photoIndex)

    val statusText: TextView = findView(R.id.status_text)
    statusText setText
      String.format("%d/%d", (photoIndex + 1).asInstanceOf[AnyRef],
      mPhotoCount.asInstanceOf[AnyRef])
  }
}

object MultiRes {
  private val mPhotoIds = Array(
    R.drawable.sample_0, R.drawable.sample_1,
    R.drawable.sample_2, R.drawable.sample_3,
    R.drawable.sample_4, R.drawable.sample_5,
    R.drawable.sample_6, R.drawable.sample_7)
  private val mPhotoCount = mPhotoIds.length
}
