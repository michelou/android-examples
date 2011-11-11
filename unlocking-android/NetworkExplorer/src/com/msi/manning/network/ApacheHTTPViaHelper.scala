package com.msi.manning.network

import android.app.{Activity, ProgressDialog}
import android.os.{Bundle, Handler, Message}
import android.view.View
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, Spinner, TextView}

import data.HTTPRequestHelper

import org.apache.http.client.ResponseHandler

/**
 * Android HTTP example demonstrating basic auth over Apache HttpClient 4 (using del.icio.us API) -
 * AND using custom HttpRequestHelper.
 * 
 * 
 * @author charliecollins
 * 
 */
class ApacheHTTPViaHelper extends Activity {
  import ApacheHTTPViaHelper._  // companion object

  private var urlChooser: Spinner = _
  private var button: Button = _
  private var output: TextView = _

  private var progressDialog: ProgressDialog = _

  // use a handler to update the UI (send the handler messages from other threads)
  private val handler = new Handler() {
    override def handleMessage(msg: Message) {
      progressDialog.dismiss()
      val bundleResult = msg.getData getString "RESPONSE"
      output setText bundleResult
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    // inflate the SAME view XML layout file as ApacheHTTPSimple Activity (re-use it)
    setContentView(R.layout.apache_http_simple)

    urlChooser = findViewById(R.id.apache_url).asInstanceOf[Spinner]
    val urls = new ArrayAdapter[String](this,
       android.R.layout.simple_spinner_item, Array(URL1, URL2))
    urls setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    urlChooser setAdapter urls

    button = findViewById(R.id.apachego_button).asInstanceOf[Button]
    output = findViewById(R.id.apache_output).asInstanceOf[TextView]

    button setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""
        performRequest(urlChooser.getSelectedItem.toString)
      }
    }
  }

  override def onPause() {
    super.onPause()
    if (progressDialog != null && progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  /**
   * Perform asynchronous HTTP using Apache `HttpClient` and `ResponseHandler`.
   * 
   * @param user
   * @param pass
   */
  private def performRequest(url: String) {
    val responseHandler = HTTPRequestHelper.getResponseHandlerInstance(handler)

    progressDialog = ProgressDialog.show(this, "working . . .", "performing HTTP request")

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        val helper = new HTTPRequestHelper(responseHandler)
        helper.performGet(url, null, null, null)
      }
    }.start()
  }
}

object ApacheHTTPViaHelper {
  private final val CLASSTAG = classOf[ApacheHTTPViaHelper].getSimpleName
  private final val URL1 = "http://www.comedycentral.com/rss/jokes/index.jhtml"
  private final val URL2 = "http://feeds.theonion.com/theonion/daily"
}
