package com.msi.manning.network

import android.app.Activity
import android.content.{Context, Intent}
import android.net.{ConnectivityManager, NetworkInfo}
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

class Main extends Activity {

  private var socketButton: Button = _
  private var getButton: Button = _
  private var apacheButton: Button = _
  private var apacheViaHelperButton: Button = _
  private var helperFormButton: Button = _
  private var deliciousButton: Button = _
  private var gClientLoginButton: Button = _

  private var status: TextView = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.main)

    socketButton = findViewById(R.id.main_socket_button).asInstanceOf[Button]
    socketButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[SimpleSocket]))
      }
    }

    getButton = findViewById(R.id.main_get_button).asInstanceOf[Button]
    getButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[SimpleGet]))
      }
    }

    apacheButton = findViewById(R.id.main_apache_button).asInstanceOf[Button]
    apacheButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[ApacheHTTPSimple]))
      }
    }

    apacheViaHelperButton = findViewById(R.id.main_apacheviahelper_button).asInstanceOf[Button]
    apacheViaHelperButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[ApacheHTTPViaHelper]))
      }
    }

    helperFormButton = findViewById(R.id.main_helper_button).asInstanceOf[Button]
    helperFormButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[HTTPHelperForm]))
      }
    }

    deliciousButton = findViewById(R.id.main_delicious_button).asInstanceOf[Button]
    deliciousButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
         startActivity(new Intent(Main.this, classOf[DeliciousRecentPosts]))
      }
    }

    gClientLoginButton = findViewById(R.id.main_gclientlogin_button).asInstanceOf[Button]
    gClientLoginButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[GoogleClientLogin]))
      }
    }

    status = findViewById(R.id.main_status).asInstanceOf[TextView]
  }

  override def onStart() {
    super.onStart()

    val cMgr = getSystemService(Context.CONNECTIVITY_SERVICE).asInstanceOf[ConnectivityManager]
    val netInfo = cMgr.getActiveNetworkInfo
    status setText (if (netInfo != null) netInfo.toString else "Network unavailable")
  }

  override def onPause() {
    super.onPause()
  }
}
