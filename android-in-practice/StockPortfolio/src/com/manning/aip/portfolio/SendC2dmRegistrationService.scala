package com.manning.aip.portfolio

import android.app.IntentService
import android.content.Intent

import SendC2dmRegistrationService._

class SendC2dmRegistrationService extends IntentService(WORKER_NAME) {

  override protected def onHandleIntent(intent: Intent) {
    try {
      val regId = intent getStringExtra "regId"
      // TODO: Send the regId to the server
    } finally {
      AlarmReceiver.releaseLock()
    }
  }
}

object SendC2dmRegistrationService {
  private final val WORKER_NAME = "SendC2DMReg"
}

