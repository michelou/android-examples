package com.manning.aip.mymoviesdatabase

import android.graphics.Bitmap
import android.widget.ImageView

import util.ImageCache

// pass in the cache so we can populate it as we go
// (using long as id/position so we can use it from both MovieAdapter and MovieCursorAdapter)
class DownloadListViewTask(cache: ImageCache, id: Long, imageView: ImageView)
extends DownloadTask(cache, imageView) {

  override protected def onPostExecute(bitmap: Bitmap) {
    val forPosition = imageView.getTag.asInstanceOf[Long]
    if ((forPosition == this.id) && (bitmap != null))
      imageView setImageBitmap bitmap
  }
}
