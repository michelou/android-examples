package com.msi.manning.network

import android.app.{Activity, ProgressDialog}
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView}

import data.HTTPRequestHelper
import data.xml.{DeliciousHandler, DeliciousPost}

import org.apache.http.client.ResponseHandler
import org.xml.sax.{InputSource, XMLReader}

import java.io.StringReader

import javax.xml.parsers.{SAXParser, SAXParserFactory}

/**
 * Android HTTP example demonstrating basic auth over Apache HttpClient 4 (using del.icio.us API),
 * and XML parsing (HTTP and Plain XML - POX).
 * 
 * 
 * @author charliecollins
 * 
 */
class DeliciousRecentPosts extends Activity {
  import DeliciousRecentPosts._  // companion object

  private var user: EditText = _
  private var pass: EditText = _
  private var output: TextView = _
  private var button: Button = _

  private var progressDialog: ProgressDialog = _

  // use a handler to update the UI (send the handler messages from other threads)
  private val handler = new Handler() {
    override def handleMessage(msg: Message) {
      progressDialog.dismiss()
      val bundleResult = msg.getData getString "RESPONSE"
      output setText parseXMLResult(bundleResult)
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.delicious_posts)

    user = findViewById(R.id.del_user).asInstanceOf[EditText]
    pass = findViewById(R.id.del_pass).asInstanceOf[EditText]
    button = findViewById(R.id.delgo_button).asInstanceOf[Button]
    output = findViewById(R.id.del_output).asInstanceOf[TextView]

    button setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""
        performRequest(user.getText.toString, pass.getText.toString)
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
  private def performRequest(user: String, pass: String) {
    progressDialog = ProgressDialog.show(this, "working . . .",
      "performing HTTP post to del.icio.us")

    val responseHandler = HTTPRequestHelper.getResponseHandlerInstance(handler)

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        val helper = new HTTPRequestHelper(responseHandler)
        helper.performPost(DeliciousRecentPosts.URL_GET_POSTS_RECENT, user, pass, null, null)
      }
    }.start()
  }

  /**
   * Parse XML result into data objects.
   * 
   * @param xmlString
   * @return
   */
  private def parseXMLResult(xmlString: String): String = {
    val result = new StringBuilder()
    try {
      val spf = SAXParserFactory.newInstance()
      val sp = spf.newSAXParser()
      val xr = sp.getXMLReader()
      val handler = new DeliciousHandler()
      xr setContentHandler handler
      xr.parse(new InputSource(new StringReader(xmlString)))

      val posts = handler.getPosts
      for (p <- posts) {
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " DeliciousPost - " + p.href)
        result append ("\n" + p.href)
      }
    } catch {
      case e: Exception =>
        Log.e(Constants.LOGTAG, " " + CLASSTAG + " ERROR - " + e);
    }
    result.toString
  }

}


object DeliciousRecentPosts {
  private final val CLASSTAG = classOf[DeliciousRecentPosts].getSimpleName
  private final val URL_GET_POSTS_RECENT = "https://api.del.icio.us/v1/posts/recent?"
}
