package com.manning.aip

import java.net.URL

import android.content.res.Resources
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.widget.ImageView

class DownloadTask(position: Int, imageView: ImageView) extends AsyncTask[/*String*/AnyRef, /*Void*/AnyRef, Bitmap] {

  private val placeholder = {
    val resources = imageView.getContext.getResources
    resources getDrawable android.R.drawable.gallery_thumb
  }

  override protected def onPreExecute() {
    imageView setImageDrawable placeholder
  }

  override protected def doInBackground(inputUrls: /*String*/AnyRef*): Bitmap =
    try {
      val url = new URL(inputUrls(0).asInstanceOf[String])
      BitmapFactory.decodeStream(url.openStream())
    } catch {
      case e: Exception =>
        e.printStackTrace()
        null
    }

  override protected def onPostExecute(result: Bitmap) {
    val forPosition = imageView.getTag.asInstanceOf[Int]
    if (forPosition == position) imageView setImageBitmap result
  }
}
