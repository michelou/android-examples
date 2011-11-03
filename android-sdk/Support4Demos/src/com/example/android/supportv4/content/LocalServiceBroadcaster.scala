/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.supportv4.content

import com.example.android.supportv4.R

import android.app.{Activity, Service}
import android.content.{BroadcastReceiver, Context, Intent, IntentFilter}
import android.os.{Bundle, Handler, IBinder, Message}
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

/**
 * Demonstrates the use of a LocalBroadcastManager to easily communicate
 * data from a service to any other interested code.
 */
class LocalServiceBroadcaster extends Activity {
  import LocalServiceBroadcaster._  // companion object

  private var mLocalBroadcastManager: LocalBroadcastManager = _
  private var mReceiver: BroadcastReceiver = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.local_service_broadcaster)

    // This is where we print the data we get back.
    val callbackData = findViewById(R.id.callback).asInstanceOf[TextView]

    // Put in some initial text.
    callbackData setText "No broadcast received yet"

    // We use this to send broadcasts within our local process.
    mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)

    // We are going to watch for interesting local broadcasts.
    val filter = new IntentFilter()
    filter addAction ACTION_STARTED
    filter addAction ACTION_UPDATE
    filter addAction ACTION_STOPPED
    mReceiver = new BroadcastReceiver() {
      override def onReceive(context: Context, intent: Intent) {
        if (intent.getAction equals ACTION_STARTED)
          callbackData setText "STARTED"
        else if (intent.getAction equals ACTION_UPDATE)
          callbackData setText ("Got update: " + intent.getIntExtra("value", 0))
        else if (intent.getAction equals ACTION_STOPPED)
          callbackData setText "STOPPED"
      }
    }
    mLocalBroadcastManager.registerReceiver(mReceiver, filter)

    // Watch for button clicks.
    var button = findViewById(R.id.start).asInstanceOf[Button]
    button setOnClickListener mStartListener
    button = findViewById(R.id.stop).asInstanceOf[Button]
    button setOnClickListener mStopListener
  }

  override protected def onDestroy() {
    super.onDestroy()
    mLocalBroadcastManager unregisterReceiver mReceiver
  }

  private val mStartListener = new OnClickListener() {
    def onClick(v: View) {
      startService(new Intent(LocalServiceBroadcaster.this, classOf[LocalService]))
    }
  }

  private val mStopListener = new OnClickListener() {
    def onClick(v: View) {
      stopService(new Intent(LocalServiceBroadcaster.this, classOf[LocalService]))
    }
  }

}

object LocalServiceBroadcaster {
  final val ACTION_STARTED = "com.example.android.supportv4.STARTED"
  final val ACTION_UPDATE = "com.example.android.supportv4.UPDATE"
  final val ACTION_STOPPED = "com.example.android.supportv4.STOPPED"

  private object LocalService {
    final val MSG_UPDATE = 1
  }

  private class LocalService extends Service {
    import LocalService._  // companion object

    private var mLocalBroadcastManager: LocalBroadcastManager = _
    private var mCurUpdate: Int = _

    private val mHandler = new Handler() {
      override def handleMessage(msg: Message) {
        if (msg.what == MSG_UPDATE) {
          mCurUpdate += 1
          val intent = new Intent(ACTION_UPDATE)
          intent.putExtra("value", mCurUpdate)
          mLocalBroadcastManager sendBroadcast intent
          sendMessageDelayed(obtainMessage(MSG_UPDATE), 1000)
        }
        else
          super.handleMessage(msg)
      }
    }

    override def onCreate() {
      super.onCreate()
      mLocalBroadcastManager = LocalBroadcastManager.getInstance(this)
    }

    override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
      // Tell any local interested parties about the start.
      mLocalBroadcastManager sendBroadcast new Intent(ACTION_STARTED)

      // Prepare to do update reports.
      mHandler removeMessages MSG_UPDATE
      val msg = mHandler obtainMessage MSG_UPDATE
      mHandler.sendMessageDelayed(msg, 1000)
      Service.START_STICKY
    }

    override def onDestroy() {
      super.onDestroy()

      // Tell any local interested parties about the stop.
      mLocalBroadcastManager sendBroadcast new Intent(ACTION_STOPPED)

      // Stop doing updates.
      mHandler removeMessages MSG_UPDATE
    }

    override def onBind(intent: Intent): IBinder = null
  }
}

