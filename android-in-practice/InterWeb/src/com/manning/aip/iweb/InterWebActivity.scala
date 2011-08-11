package com.manning.aip.iweb

import java.io.File

import android.accounts.{Account, AccountManager}
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.{ContactsContract, MediaStore}
import android.util.Log
import android.webkit.ConsoleMessage.MessageLevel.ERROR
import android.webkit.{ConsoleMessage, JsResult, WebChromeClient}
import android.webkit.{WebSettings, WebView, WebViewClient}

import scala.android.provider.ContactsContract.Contacts

class InterWebActivity extends Activity {
  import Activity._, InterWebActivity._  // companion object

  private var webView: WebView = _
  private var webInterface: InterWebInterface = _
  private var onResumeCount = 0

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    webView = findViewById(R.id.web).asInstanceOf[WebView]
    val settings = webView.getSettings
    settings setJavaScriptEnabled true
    webInterface = new InterWebInterface()
    webView.addJavascriptInterface(webInterface, "android")
    webView setWebChromeClient new WebChromeClient {
      override def onJsAlert(view: WebView, url: String, message: String,
                    result: JsResult): Boolean = {
        Log.d(LOG_TAG, "WebView JsAlert message = %s %s".format(url, message))
        false
      }

      override def onConsoleMessage(consoleMessage: ConsoleMessage): Boolean = {
        val msg = new StringBuilder(consoleMessage
                        .messageLevel.name).append('\t')
                        .append(consoleMessage.message).append('\t')
                        .append(consoleMessage.sourceId).append(" (")
                        .append(consoleMessage.lineNumber).append(")\n")
        if (consoleMessage.messageLevel == ERROR)
          Log.e(LOG_TAG, msg.toString)
        else
          Log.d(LOG_TAG, msg.toString)
        true
      }
    }
    webView setWebViewClient new WebViewClient {
      override def shouldOverrideUrlLoading(view: WebView, url: String): Boolean = {
        Log.d(LOG_TAG, "Loading url=" + url)
        false
      }
    }
    webView loadUrl "file:///android_asset/interweb.html"
    onCreateCount += 1
  }

  override protected def onResume() {
    super.onResume();
    onResumeCount += 1
  }

  override protected def onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == RESULT_OK) {
      if (requestCode == REQUEST_CONTACT)
        webInterface executeContactCallback data.getData
      if (requestCode == REQUEST_PIC)
        webInterface executePicCallback data.getData
    }
  }

  private def getContactDisplayName(contactUri: Uri): String = {
    val projection = Array(Contacts._ID, Contacts.DISPLAY_NAME)
    val cursor = managedQuery(contactUri, projection, null, null, null)
    if (cursor.moveToNext())
      cursor getString cursor.getColumnIndex(Contacts.DISPLAY_NAME)
    else
      "Couldn't find it"
  }

  private def getPictureData(pictureUri: Uri): String = {
    val projection = Array(MediaStore.MediaColumns.DATA)
    val cursor = managedQuery(pictureUri, projection, null, null, null)
    if (cursor.moveToNext())
      cursor getString cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
    else
      ""
  }

  // see resource assets/interweb.html
  class InterWebInterface {
    var callback: String = _

    def getCreateCount: String = onCreateCount.toString

    def getResumeCount: String = onResumeCount.toString

    def getUserName: String = {
      val mgr = AccountManager get InterWebActivity.this
      val gAccount = mgr.getAccountsByType("com.google")(0)
      gAccount.name
    }

    def selectContact(callback: String) {
      this.callback = callback
      val intentContact = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI)
      startActivityForResult(intentContact, REQUEST_CONTACT)
    }

    def selectPicture(callback: String) {
      this.callback = callback
      val intentPicture = new Intent(Intent.ACTION_GET_CONTENT)
      intentPicture setType "image/*"
      startActivityForResult(intentPicture, REQUEST_PIC)
    }

    /*protected*/ def executeContactCallback(contact: Uri) {
      val name = getContactDisplayName(contact)
      webView loadUrl "javascript:contactCallback('%s')".format(name)
    }

    /*protected*/ def executePicCallback(picture: Uri) {
      val filePath = getPictureData(picture)
      val f = new File(filePath)
      val uri = Uri.fromFile(f).toString
      webView loadUrl "javascript:pictureCallback('%s')".format(uri)
    }
  }
}

object InterWebActivity {
  private final val REQUEST_PIC = 5
  private final val REQUEST_CONTACT = 4
  private final val LOG_TAG = "InterWebActivity"

  private var onCreateCount = 0
}

