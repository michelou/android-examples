package com.msi.manning.network

import android.app.{Activity, ProgressDialog}
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, Spinner, TextView}

import util.StringUtils

import org.apache.http.{HttpEntity, HttpResponse, StatusLine}
import org.apache.http.client.ClientProtocolException
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import java.io.IOException

/**
 * Android basic HTTP example using Apache HttpClient.
 * 
 * 
 * @author charliecollins
 * 
 */
class ApacheHTTPSimple extends Activity {
  import ApacheHTTPSimple._  // companion object

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
        performRequest()
      }
    }
  }

  override def onPause() {
    super.onPause();
    if (progressDialog != null && progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  /**
   * Perform asynchronous HTTP using Apache <code>HttpClient</code> and
   * <code>ResponseHandler</code>.
   * 
   * @param user
   * @param pass
   */
  private def performRequest() {

    // use a response handler so we aren't blocking on the HTTP request
    val responseHandler = new ResponseHandler[String]() {

      def handleResponse(response: HttpResponse): String = {
        // when the response happens close the notification and update UI
        val status = response.getStatusLine
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " statusCode - " + status.getStatusCode)
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " statusReasonPhrase - "
                    + status.getReasonPhrase)
        val entity = response.getEntity
        var result: String = null
        try {
          result = StringUtils.inputStreamToString(entity.getContent)
          val message = handler.obtainMessage()
          val bundle = new Bundle()
          bundle.putString("RESPONSE", result)
          message setData bundle
          handler sendMessage message
        } catch {
          case e: IOException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        }
        result
      }
    }

    progressDialog = ProgressDialog.show(this, "working . . .", "performing HTTP request")

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        try {
          val client = new DefaultHttpClient()
          val httpMethod = new HttpGet(urlChooser.getSelectedItem.toString)
          client.execute(httpMethod, responseHandler)
        } catch {
          case e: ClientProtocolException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
          case e: IOException =>
            Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
        }
      }
    }.start();
  }

}

object ApacheHTTPSimple {
  private final val CLASSTAG = classOf[ApacheHTTPSimple].getSimpleName
  private final val URL1 = "http://www.comedycentral.com/rss/jokes/index.jhtml"
  private final val URL2 = "http://feeds.theonion.com/theonion/daily"
}
