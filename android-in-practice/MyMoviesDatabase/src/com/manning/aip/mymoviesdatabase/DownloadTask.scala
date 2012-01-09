package com.manning.aip.mymoviesdatabase

import android.content.res.Resources
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.util.Log
import android.widget.ImageView

import util.{ImageCache, ImageUtil}

import java.io.IOException
import java.net.{MalformedURLException, URL, URLConnection}

class DownloadTask(cache: ImageCache, imageView: ImageView)
extends AsyncTask[/*String*/AnyRef, /*Void*/AnyRef, Bitmap] {

  private val placeholder: Drawable = {
    val resources = imageView.getContext.getResources
    resources getDrawable android.R.drawable.gallery_thumb
  }

  override protected def onPreExecute() {
    imageView setImageDrawable placeholder
  }

  override protected def doInBackground(inputUrls: AnyRef*): Bitmap = {
    val imageUrl = inputUrls(0).toString
    Log.d(Constants.LOG_TAG, "making HTTP trip for image:" + imageUrl)
    var bitmap: Bitmap = null
    try {
      // NOTE, be careful about just doing "url.openStream()"
      // it's a shortcut for openConnection().getInputStream() and doesn't set timeouts
      // (the defaults are "infinite" so it will wait forever if endpoint server is down)
      // do it properly with a few more lines of code . . .
      val url = new URL(imageUrl)
      val conn = url.openConnection()
      conn setConnectTimeout 3000
      conn setReadTimeout 5000
      bitmap = BitmapFactory.decodeStream(conn.getInputStream)
      if (bitmap != null) {
        bitmap = ImageUtil.getRoundedCornerBitmap(bitmap, 12)
        cache.put(imageUrl, bitmap)
      }
    } catch {
      case e: MalformedURLException =>
        Log.e(Constants.LOG_TAG, "Exception loading image, malformed URL", e)
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "Exception loading image, IO error", e)
    }
    bitmap
  }

  override protected def onPostExecute(bitmap: Bitmap) {
    if (bitmap != null) imageView setImageBitmap bitmap
  }
}
