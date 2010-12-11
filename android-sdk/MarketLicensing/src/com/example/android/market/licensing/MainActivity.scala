/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.example.android.market.licensing

import android.app.{AlertDialog, Dialog}
import android.content.{DialogInterface, Intent}
import android.net.Uri
import android.os.{Bundle, Handler}
import android.provider.Settings.Secure
import android.view.{View, Window}
import android.widget.{Button, TextView}

import com.android.vending.licensing.AESObfuscator
import com.android.vending.licensing.LicenseChecker
import com.android.vending.licensing.LicenseCheckerCallback
import com.android.vending.licensing.LicenseCheckerCallback.ApplicationErrorCode
import com.android.vending.licensing.ServerManagedPolicy

import scala.android.app.Activity

/**
 * Welcome to the world of Android Market licensing. We're so glad to have you
 * onboard!
 * <p>
 * The first thing you need to do is get your hands on your public key.
 * Update the BASE64_PUBLIC_KEY constant below with your encoded public key,
 * which you can find on the
 * <a href="http://market.android.com/publish/editProfile">Edit Profile</a>
 * page of the Market publisher site.
 * <p>
 * Log in with the same account on your Cupcake (1.5) or higher phone or
 * your FroYo (2.2) emulator with the Google add-ons installed. Change the
 * test response on the Edit Profile page, press Save, and see how this
 * application responds when you check your license.
 * <p>
 * After you get this sample running, peruse the
 * <a href="http://developer.android.com/guide/publishing/licensing.html">
 * licensing documentation.</a>
 */
class MainActivity extends Activity {
  import MainActivity._  // companion object

  private var mStatusText: TextView = _
  private var mCheckLicenseButton: Button = _

  private var mLicenseCheckerCallback: LicenseCheckerCallback = _
  private var mChecker: LicenseChecker = _
  // A handler on the UI thread.
  private var mHandler: Handler = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    setContentView(R.layout.main)

    mStatusText = findView(R.id.status_text)
    mCheckLicenseButton = findView(R.id.check_license_button)
    mCheckLicenseButton setOnClickListener {
      doCheck()
    }

    mHandler = new Handler()

    // Try to use more data here. ANDROID_ID is a single point of attack.
    val deviceId = Secure.getString(getContentResolver, Secure.ANDROID_ID)

    // Library calls this when it's done.
    mLicenseCheckerCallback = new MyLicenseCheckerCallback()
    // Construct the LicenseChecker with a policy.
    mChecker = new LicenseChecker(
      this, new ServerManagedPolicy(this,
        new AESObfuscator(SALT, getPackageName, deviceId)),
        BASE64_PUBLIC_KEY)
    doCheck()
  }

  override protected def onCreateDialog(id: Int): Dialog = {
    // We have only one dialog.
    new AlertDialog.Builder(this)
      .setTitle(R.string.unlicensed_dialog_title)
      .setMessage(R.string.unlicensed_dialog_body)
      .setPositiveButton(R.string.buy_button,
        new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {
            val marketIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
               "http://market.android.com/details?id=" + getPackageName))
            startActivity(marketIntent)
          }
        })
      .setNegativeButton(R.string.quit_button,
        new DialogInterface.OnClickListener() {
          def onClick(dialog: DialogInterface, which: Int) {
            finish()
          }
        })
      .create()
  }

  private def doCheck() {
    mCheckLicenseButton setEnabled false
    setProgressBarIndeterminateVisibility(true)
    mStatusText setText R.string.checking_license
    mChecker checkAccess mLicenseCheckerCallback
  }

  private def displayResult(result: String) {
    mHandler post new Runnable() {
      override def run() {
        mStatusText setText result
        setProgressBarIndeterminateVisibility(false)
        mCheckLicenseButton setEnabled true
      }
    }
  }

  private class MyLicenseCheckerCallback extends LicenseCheckerCallback {
    def allow() {
      if (isFinishing) {
        // Don't update UI if Activity is finishing.
        return
      }
      // Should allow user access.
      displayResult(getString(R.string.allow))
    }

    def dontAllow() {
      if (isFinishing()) {
        // Don't update UI if Activity is finishing.
        return
      }
      displayResult(getString(R.string.dont_allow))
      // Should not allow access. In most cases, the app should assume
      // the user has access unless it encounters this. If it does,
      // the app should inform the user of their unlicensed ways
      // and then either shut down the app or limit the user to a
      // restricted set of features.
      // In this example, we show a dialog that takes the user to Market.
      showDialog(0)
    }

    def applicationError(errorCode: ApplicationErrorCode) {
      if (isFinishing()) {
        // Don't update UI if Activity is finishing.
        return
      }
      // This is a polite way of saying the developer made a mistake
      // while setting up or calling the license checker library.
      // Please examine the error code and fix the error.
      val result = String.format(getString(R.string.application_error), errorCode)
      displayResult(result)
    }
  }

  override protected def onDestroy() {
    super.onDestroy()
    mChecker.onDestroy()
  }

}

object MainActivity {

  private final val BASE64_PUBLIC_KEY = "REPLACE THIS WITH YOUR PUBLIC KEY"

  // Generate your own 20 random bytes, and put them here.
  private final val SALT = Array[Byte](
    -46, 65, 30, -128, -103, -57, 74, -64, 51, 88,
    -95, -45, 77, -117, -36, -113, -11, 32, -64, 89
  )

}
