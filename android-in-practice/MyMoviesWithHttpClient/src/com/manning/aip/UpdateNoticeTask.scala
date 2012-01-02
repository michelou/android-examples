package com.manning.aip

import org.apache.http.{HttpResponse, HttpStatus}
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils

import android.os.{AsyncTask, Bundle, Handler, Message}

class UpdateNoticeTask(handler: Handler) extends AsyncTask[/*Void*/AnyRef, /*Void*/AnyRef, String] {
  import UpdateNoticeTask._ // companion object

  override protected def doInBackground(params: AnyRef*): String =
    try {
      val request = new HttpGet(UPDATE_URL)
      request.setHeader("Accept", "text/plain")
      val response = MyMovies.getHttpClient execute request
      val statusCode = response.getStatusLine.getStatusCode
      if (statusCode != HttpStatus.SC_OK)
        "Error: Failed getting update notes"
      else
        EntityUtils.toString(response.getEntity)
    } catch {
      case e: Exception =>
        "Error: " + e.getMessage
    }

  override protected def onPostExecute(updateNotice: String) {
    val message = new Message()
    val data = new Bundle()
    data.putString("text", updateNotice)
    message setData data
    handler sendMessage message
  }
}

object UpdateNoticeTask {
  private val UPDATE_URL =
            "http://android-in-practice.googlecode.com/files/update_notice.txt"
}
