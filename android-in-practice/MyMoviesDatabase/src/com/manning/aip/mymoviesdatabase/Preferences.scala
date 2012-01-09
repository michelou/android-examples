package com.manning.aip.mymoviesdatabase

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.preference.{CheckBoxPreference, PreferenceActivity, PreferenceManager}

class Preferences extends PreferenceActivity {

  private var showSplash: CheckBoxPreference = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    addPreferencesFromResource(R.layout.preferences)

    // handle to preferences doesn't come from findViewById!
    showSplash = (getPreferenceScreen findPreference "showsplash").asInstanceOf[CheckBoxPreference]

    setCheckBoxSummary(showSplash)

    // listen to see if user changes pref, so we can update display of current value
    val prefs = PreferenceManager.getDefaultSharedPreferences(this)
    prefs registerOnSharedPreferenceChangeListener new OnSharedPreferenceChangeListener() {
      def onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        if (key equals "showsplash") setCheckBoxSummary(showSplash)
      }
    }
  }

  private def setCheckBoxSummary(pref: CheckBoxPreference) {
    pref.setSummary(if (pref.isChecked) "Enabled" else "Disabled")
  }
}
