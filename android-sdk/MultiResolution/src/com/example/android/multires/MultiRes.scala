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

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.{Button, ImageView, TextView}

final class MultiRes extends Activity {
  import MultiRes._  // companion object

  private var mCurrentPhotoIndex = 0

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    showPhoto(mCurrentPhotoIndex)

    // Handle clicks on the 'Next' button.
    val nextButton = findViewById(R.id.next_button).asInstanceOf[Button]
    nextButton setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        mCurrentPhotoIndex = (mCurrentPhotoIndex + 1) % mPhotoCount
        showPhoto(mCurrentPhotoIndex)
      }
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
    val imageView = findViewById(R.id.image_view).asInstanceOf[ImageView]
    imageView setImageResource mPhotoIds(photoIndex)

    val statusText = findViewById(R.id.status_text).asInstanceOf[TextView]
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
