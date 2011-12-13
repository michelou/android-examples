/*
 * Copyright (C) 2008 Google Inc.
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
package com.google.android.divideandconquer

import android.content.{Context, SharedPreferences}
import android.os.Bundle
import android.preference._

/**
 * Holds preferences for the game
 */
class Preferences extends PreferenceActivity {
  import Preferences._  // companion object

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setPreferenceScreen(createPreferenceHierarchy())
  }

  private def createPreferenceHierarchy(): PreferenceScreen = {
    // Root
    val root = getPreferenceManager.createPreferenceScreen(this)

    // vibrate on/off
    val vibratePref = new CheckBoxPreference(this)
    vibratePref setDefaultValue true
    vibratePref setKey KEY_VIBRATE
    vibratePref setTitle R.string.settings_vibrate
    vibratePref setSummary R.string.settings_vibrate_summary
    root addPreference vibratePref

    // difficulty level
    val difficultyPref = new ListPreference(this)
    difficultyPref setEntries Array[java.lang.CharSequence](
                getString(R.string.settings_five_lives),
                getString(R.string.settings_three_lives),
                getString(R.string.settings_one_life))
    difficultyPref setEntryValues Array[java.lang.CharSequence](
                Difficulty.Easy.toString,
                Difficulty.Medium.toString,
                Difficulty.Hard.toString)
    difficultyPref setKey KEY_DIFFICULTY
    difficultyPref setTitle R.string.settings_difficulty
    difficultyPref setSummary R.string.settings_difficulty_summary
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
    if (!sharedPrefs.contains(KEY_DIFFICULTY)) {
      difficultyPref setValue DEFAULT_DIFFICULTY.toString
    }
    root addPreference difficultyPref
    root
  }
}

object Preferences {
  final val KEY_VIBRATE = "key_vibrate"
  final val KEY_DIFFICULTY = "key_difficulty"

  object Difficulty extends Enumeration {
    val Easy   = DiffValue("Easy", 5)
    val Medium = DiffValue("Medium", 3)
    val Hard   = DiffValue("Hard", 1)

    case class DiffValue(shortName: String, livesToStart: Int) extends Val(shortName) {
      def getLivesToStart: Int =  livesToStart
    }
    def valueOf(shortName: String): DiffValue = shortName match {
      case "Easy"   => Easy
      case "Medium" => Medium
      case "Hard"   => Hard
      case _ => throw new java.util.NoSuchElementException()
    }
  }
  type Difficulty = Difficulty.DiffValue

  def getCurrentDifficulty(context: Context): Difficulty = {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val diffic = preferences.getString(KEY_DIFFICULTY, DEFAULT_DIFFICULTY.toString)
    Difficulty.valueOf(diffic)
  }

  final val DEFAULT_DIFFICULTY = Difficulty.Medium
}
