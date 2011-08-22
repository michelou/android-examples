/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.browserintent

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.{KeyEvent, View}
import android.view.View.{OnClickListener, OnKeyListener}
import android.widget.{Button, EditText, TextView}

class BrowserIntent extends Activity {
  private var urlText: EditText = _
  private var goButton: Button = _
  private var errMsg: TextView = _ //mics

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    // Get a handle to all user interface elements
    urlText = findViewById(R.id.url_field).asInstanceOf[EditText]
    goButton = findViewById(R.id.go_button).asInstanceOf[Button]
    errMsg = findViewById(R.id.err_msg).asInstanceOf[TextView]

    // Setup event handlers
    goButton setOnClickListener new OnClickListener() { 
      def onClick(view: View) {
        openBrowser()
      }
    }
    urlText setOnKeyListener new OnKeyListener() { 
      def onKey(view: View, keyCode: Int, event: KeyEvent): Boolean = {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          openBrowser()
          true
        } else
         false
      }
    }
  }

  /** Open a browser on the URL specified in the text box */
  private def openBrowser() {
    val uri = Uri.parse(urlText.getText.toString)
    val intent = new Intent(Intent.ACTION_VIEW, uri)
    try startActivity(intent)
    catch { case e: Exception => errMsg setText e.getMessage }
  }
}
