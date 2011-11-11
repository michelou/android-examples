package com.msi.manning.network

import android.app.{Activity, ProgressDialog}
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{ArrayAdapter, Button, EditText, Spinner, TextView}

import data.HTTPRequestHelper

import org.apache.http.client.ResponseHandler

/**
 * Simple form to exercise the HttpRequestHelper class (which wraps HttpClient).
 * 
 * GET: http://www.yahoo.com GET: http://www.google.com/search?&q=android POST:
 * http://www.snee.com/xml/crud/posttest.cgi (fname and lname params)
 * 
 * Or, host "echo.jsp" from the root of this project locally and use it for testing.
 * 
 * 
 * @author charliecollins
 * 
 */
class HTTPHelperForm extends Activity {
  import HTTPHelperForm._  // companion object

  private var url: EditText = _
  private var method: Spinner = _
  private var param1Name: EditText = _
  private var param1Value: EditText = _
  private var param2Name: EditText = _
  private var param2Value: EditText = _
  private var param3Name: EditText = _
  private var param3Value: EditText = _
  private var user: EditText = _
  private var pass: EditText = _
  private var go: Button = _
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
    // inflate the SAME view XML layout file as ApacheHTTPWithAuth Activity (re-use it)
    setContentView(R.layout.http_helper_form)

    url = findViewById(R.id.htth_url).asInstanceOf[EditText]
    method = findViewById(R.id.htth_method).asInstanceOf[Spinner]
    param1Name = findViewById(R.id.htth_param1_name).asInstanceOf[EditText]
    param1Value = findViewById(R.id.htth_param1_value).asInstanceOf[EditText]
    param2Name = findViewById(R.id.htth_param2_name).asInstanceOf[EditText]
    param2Value = findViewById(R.id.htth_param2_value).asInstanceOf[EditText]
    param3Name = findViewById(R.id.htth_param3_name).asInstanceOf[EditText]
    param3Value = findViewById(R.id.htth_param3_value).asInstanceOf[EditText]
    user = findViewById(R.id.htth_user).asInstanceOf[EditText]
    pass = findViewById(R.id.htth_pass).asInstanceOf[EditText]
    go = findViewById(R.id.htth_go_button).asInstanceOf[Button]
    output = findViewById(R.id.htth_output).asInstanceOf[TextView]

    val methods =
      new ArrayAdapter[String](this, android.R.layout.simple_spinner_item,
                               Array("GET", "POST"))
    methods setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    method setAdapter methods

    go setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""

        performRequest(url.getText.toString, method.getSelectedItem.toString,
                       param1Name.getText.toString, param1Value.getText.toString,
                       param2Name.getText.toString, param2Value.getText.toString,
                       param3Name.getText.toString, param3Value.getText.toString,
                       user.getText.toString, pass.getText.toString)
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
   * Perform asynchronous HTTP using Apache `HttpClient` via
   * `HttpRequestHelper` and `ResponseHandler`.
   * 
   * @param url
   * @param method
   * @param param1Name
   * @param param1Value
   * @param param2Name
   * @param param2Value
   * @param param3Name
   * @param param3Value
   * @param user
   * @param pass
   */
  private def performRequest(url: String, method: String, param1Name: String,
                             param1Value: String, param2Name: String,
                             param2Value: String, param3Name: String,
                             param3Value: String, user: String, pass: String) {

    Log.d(Constants.LOGTAG, " " + CLASSTAG + " request url - " + url)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " request method - " + method)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param1Name - " + param1Name)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param1Value - " + param1Value)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param2Name - " + param2Name)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param2Value - " + param2Value)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param3Name - " + param3Name)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " param3Value - " + param3Value)
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " user - " + user)
    if (pass != null) {
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " pass length - " + pass.length)
    }

    val params = {
      val params0 = new collection.mutable.HashMap[String, String]()
      if (param1Name != null && param1Value != null) {
        params0 += param1Name -> param1Value
      }
      if (param2Name != null && param2Value != null) {
        params0 += param2Name -> param2Value
      }
      if (param3Name != null && param3Value != null) {
        params0 += param3Name -> param3Value
      }
      params0.toMap
    }

    val responseHandler = HTTPRequestHelper.getResponseHandlerInstance(handler)

    progressDialog = ProgressDialog.show(this, "working . . .", "performing HTTP request")

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        val helper = new HTTPRequestHelper(responseHandler)
        if (method equals "GET") {
          helper.performGet(url, user, pass, null)
        } else if (method equals "POST") {
          helper.performPost(HTTPRequestHelper.MIME_FORM_ENCODED, url, user, pass, null, params)
        } else {
          val msg = handler.obtainMessage()
          val bundle = new Bundle()
          bundle.putString("RESPONSE", "ERROR - see logcat")
          msg setData bundle
          handler sendMessage msg
          Log.w(Constants.LOGTAG, " " + CLASSTAG + " unknown method, nothing to do")
        }
      }
    }.start()
  }
}

object HTTPHelperForm {
  private final val CLASSTAG = classOf[HTTPHelperForm].getSimpleName
}
