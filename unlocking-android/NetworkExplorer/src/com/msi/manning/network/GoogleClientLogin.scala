package com.msi.manning.network

import android.app.{Activity, ProgressDialog}
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView}

import data.HTTPRequestHelper

import org.apache.http.client.ResponseHandler

import java.io.UnsupportedEncodingException
import java.net.URLEncoder

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap

/**
 * Android Apache HTTP example demonstrating using the ClientLogin feature of the Google Data APIs
 * (obtain a token, and send it as a header with subsequent requests).
 * 
 * @author charliecollins
 * 
 */
class GoogleClientLogin extends Activity {
  import GoogleClientLogin._  // companion object

  private var tokenValue: String = _

  private var emailAddress: EditText = _
  private var password: EditText = _
  private var getContacts: Button = _
  private var getToken: Button = _
  private var clearToken: Button = _
  private var tokenText: TextView = _
  private var output: TextView = _

  private var progressDialog: ProgressDialog = _

  private val tokenHandler = new Handler() {
    override def handleMessage(msg: Message) {
      progressDialog.dismiss()
      val bundleResult = msg.getData getString "RESPONSE"
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " response body before auth trim - "
                + bundleResult)

      var authToken = bundleResult
      authToken = authToken.substring(authToken.indexOf("Auth=") + 5, authToken.length).trim()

      tokenValue = authToken
      tokenText setText authToken
    }
  }

  private val contactsHandler = new Handler() {
    override def handleMessage(msg: Message) {
      progressDialog.dismiss()
      val bundleResult = msg.getData getString "RESPONSE"
      output setText bundleResult
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.google_client_login)

    emailAddress = findViewById(R.id.gclient_email).asInstanceOf[EditText]
    password = findViewById(R.id.gclient_password).asInstanceOf[EditText]

    getToken = findViewById(R.id.gclientgettoken_button).asInstanceOf[Button]
    clearToken = findViewById(R.id.gclientcleartoken_button).asInstanceOf[Button]
    getContacts = findViewById(R.id.gclientgetcontacts_button).asInstanceOf[Button]
    tokenText = findViewById(R.id.gclient_token).asInstanceOf[TextView]
    output = findViewById(R.id.gclient_output).asInstanceOf[TextView]

    getToken setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""
        if (tokenValue == null || tokenValue.equals("")) {
          Log.d(Constants.LOGTAG, " " + CLASSTAG + " token NOT set, getting it")
          getToken(emailAddress.getText.toString, password.getText.toString)
        } else {
          Log.d(Constants.LOGTAG, " " + CLASSTAG +
                 " token already set, not re-getting it (clear it first)")
        }
      }
    }

    clearToken setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""
        tokenText setText ""
        tokenValue = null
      }
    }

    getContacts setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        output setText ""
        getContacts(emailAddress.getText.toString, tokenValue)
      }
    }
  }

  override def onPause() {
    super.onPause()
    if (progressDialog != null && progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  private def getContacts(email: String, token: String) {
    val responseHandler =
      HTTPRequestHelper.getResponseHandlerInstance(contactsHandler)

    progressDialog = ProgressDialog.show(this, "working . . .", "getting Google Contacts")

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        val headers = Map[String, String](
          GTOKEN_AUTH_HEADER_NAME -> (GTOKEN_AUTH_HEADER_VALUE_PREFIX + token)
        )
        val encEmail =
          try URLEncoder.encode(email, "UTF-8")
          catch {
            case e: UnsupportedEncodingException =>
              Log.e(Constants.LOGTAG, " " + CLASSTAG, e)
              email
          }
        val url = URL_GET_CONTACTS_PREFIX + encEmail + URL_GET_CONTACTS_SUFFIX
        Log.d(Constants.LOGTAG, " " + CLASSTAG + " getContacts URL - " + url)

        val helper = new HTTPRequestHelper(responseHandler)
        helper.performGet(url, null, null, headers)
      }
    }.start()
  }

  private def getToken(email: String, pass: String) {
    val responseHandler = HTTPRequestHelper.getResponseHandlerInstance(tokenHandler)

    progressDialog = ProgressDialog.show(this, "working . . .", "getting Google ClientLogin token")

    // do the HTTP dance in a separate thread (the responseHandler will fire when complete)
    new Thread() {
      override def run() {
        val params = Map[String, String](
          PARAM_ACCOUNT_TYPE -> PARAM_ACCOUNT_TYPE_VALUE,
          PARAM_EMAIL -> email,
          PARAM_PASSWD -> pass,
          PARAM_SERVICE -> PARAM_SERVICE_VALUE,
          PARAM_SOURCE -> PARAM_SOURCE_VALUE
        )
        val helper = new HTTPRequestHelper(responseHandler)
        helper.performPost(HTTPRequestHelper.MIME_FORM_ENCODED, URL_GET_GTOKEN, null, null,
                    null, params)
      }
    }.start()
  }
}

object GoogleClientLogin {
  private final val CLASSTAG = classOf[GoogleClientLogin].getSimpleName
  private final val URL_GET_GTOKEN = "https://www.google.com/accounts/ClientLogin"
  private final val URL_GET_CONTACTS_PREFIX = "http://www.google.com/m8/feeds/contacts/" // liz%40gmail.com
  private final val URL_GET_CONTACTS_SUFFIX = "/full"
  private final val GTOKEN_AUTH_HEADER_NAME = "Authorization"
  private final val GTOKEN_AUTH_HEADER_VALUE_PREFIX = "GoogleLogin auth="
  private final val PARAM_ACCOUNT_TYPE = "accountType"
  private final val PARAM_ACCOUNT_TYPE_VALUE = "HOSTED_OR_GOOGLE"
  private final val PARAM_EMAIL = "Email"
  private final val PARAM_PASSWD = "Passwd"
  private final val PARAM_SERVICE = "service"
  private final val PARAM_SERVICE_VALUE = "cp" // google contacts
  private final val PARAM_SOURCE = "source"
  private final val PARAM_SOURCE_VALUE = "manning-unlockingAndrid-1.0"
}
