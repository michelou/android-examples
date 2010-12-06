package com.androidbook.triviaquiz.tests

import android.content.{Context, SharedPreferences}
import android.test.ActivityInstrumentationTestCase2
import android.util.Log
import android.widget.EditText

import junit.framework.Assert._

import com.androidbook.triviaquiz._

class QuizSettingsActivityTests extends ActivityInstrumentationTestCase2[QuizSettingsActivity]("com.androidbook.triviaquiz", classOf[QuizSettingsActivity]) {
  import QuizSettingsActivityTests._ // companion object

  private var nickname: EditText = _

  @throws(classOf[Exception])
  override protected def setUp() {
    super.setUp()
    nickname = getActivity findEditText R.id.EditText_Nickname
  }

  def testNicknameFieldConsistency() {
    val settings = getActivity.getSharedPreferences(
      QuizConstants.GAME_PREFERENCES, Context.MODE_PRIVATE)
    val fromPrefs = settings.getString(QuizConstants.GAME_PREFERENCES_NICKNAME, "")
    val fromField = nickname.getText.toString
    assertTrue("Field should equal prefs value", fromPrefs equals fromField)
  }

  // ...
  def testUpdateNickname() {
    Log.w(DEBUG_TAG, "Warning: " + "If nickname was previously 'testnick' this test is invalid.")
    getActivity runOnUiThread new Runnable() {
      def run() {
        nickname setText ""
        nickname.requestFocus()
      }
    }
    sendKeys(TESTNICK_KEY_PRESSES)
    val settings = getActivity.getSharedPreferences(QuizConstants.GAME_PREFERENCES,
                Context.MODE_PRIVATE)
    val fromPrefs = settings.getString(QuizConstants.GAME_PREFERENCES_NICKNAME, "")
    assertTrue("Prefs should be testnick", fromPrefs equalsIgnoreCase "testnick")
  }
}

object QuizSettingsActivityTests {
  private final val DEBUG_TAG = "QuizSettingsActivityTests"

  private final val TESTNICK_KEY_PRESSES = "T E S T N I C K ENTER"
}

