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

import android.app.{Activity, Dialog}
import android.content.{Context, DialogInterface, Intent}
import android.os.{Bundle, SystemClock, Vibrator}
import android.preference.PreferenceManager
import android.view.{Menu, MenuItem, Window, Gravity}
import android.widget.{TextView, Toast}
import android.graphics.Color

import scala.collection.mutable.Stack

/**
 * The activity for the game.  Listens for callbacks from the game engine, and
 * response appropriately, such as bringing up a 'game over' dialog when a ball
 * hits a moving line and there is only one life left.
 */
class DivideAndConquerActivity extends Activity
                                  with DivideAndConquerView.BallEngineCallBack
                                  with NewGameCallback
                                  with DialogInterface.OnCancelListener {
  import DivideAndConquerActivity._  // companion object

  private var mVibrateOn: Boolean = _
    
  private var mNumBalls = NEW_GAME_NUM_BALLS
    
  private var mBallsView: DivideAndConquerView = _

  private var mWelcomeDialog: WelcomeDialog = _
  private var mGameOverDialog: GameOverDialog = _

  private var mLivesLeft: TextView = _
  private var mPercentContained: TextView = _
  private var mNumLives: Int = _
  private var mVibrator: Vibrator = _
  private var mLevelInfo: TextView = _
  private var mNumLivesStart = 5

  private var mCurrentToast: Toast = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    // Turn off the title bar
    requestWindowFeature(Window.FEATURE_NO_TITLE)

    setContentView(R.layout.main)
    mBallsView = findViewById(R.id.ballsView).asInstanceOf[DivideAndConquerView]
    mBallsView setCallback this

    mPercentContained = findViewById(R.id.percentContained).asInstanceOf[TextView]
    mLevelInfo = findViewById(R.id.levelInfo).asInstanceOf[TextView]
    mLivesLeft = findViewById(R.id.livesLeft).asInstanceOf[TextView]

    // we'll vibrate when the ball hits the moving line
    mVibrator = getSystemService(Context.VIBRATOR_SERVICE).asInstanceOf[Vibrator]
  }

  /** {@inheritDoc} */
  def onEngineReady(ballEngine: BallEngine) {
    // display 10 balls bouncing around for visual effect
    ballEngine.reset(SystemClock.elapsedRealtime(), 10)
    mBallsView setMode DivideAndConquerView.Mode.Bouncing

    // show the welcome dialog
    showDialog(WELCOME_DIALOG)
  }

  override protected def onCreateDialog(id: Int): Dialog =
    id match {
      case WELCOME_DIALOG =>
        mWelcomeDialog = new WelcomeDialog(this, this)
        mWelcomeDialog setOnCancelListener this
        mWelcomeDialog
      case GAME_OVER_DIALOG =>
        mGameOverDialog = new GameOverDialog(this, this)
        mGameOverDialog setOnCancelListener this
        mGameOverDialog
      case _ =>
        null
    }

  override protected def onPause() {
    super.onPause()
    mBallsView setMode DivideAndConquerView.Mode.PausedByUser
  }

  override protected def onResume() {
    super.onResume()

    mVibrateOn = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(Preferences.KEY_VIBRATE, true)

    mNumLivesStart = Preferences.getCurrentDifficulty(this).getLivesToStart
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    menu.add(0, MENU_NEW_GAME, MENU_NEW_GAME, "New Game")
    menu.add(0, MENU_SETTINGS, MENU_SETTINGS, "Settings")

    true
  }

  /**
   * We pause the game while the menu is open; this remembers what it was
   * so we can restore when the menu closes
   */
  private val mRestoreMode = new Stack[DivideAndConquerView.Mode]

  override def onMenuOpened(featureId: Int, menu: Menu): Boolean = {
    saveMode()
    mBallsView setMode DivideAndConquerView.Mode.Paused
    super.onMenuOpened(featureId, menu)
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    super.onOptionsItemSelected(item)

    item.getItemId match {
      case MENU_NEW_GAME =>
        cancelToasts()
        onNewGame()
      case MENU_SETTINGS =>
        val intent = new Intent(this, classOf[Preferences])
        startActivity(intent)
    }

    mRestoreMode.pop() // don't want to restore when an action was taken

    true
  }

  override def onOptionsMenuClosed(menu: Menu) {
    super.onOptionsMenuClosed(menu)
    restoreMode()
  }

  private def saveMode() {
    // don't want to restore to a state where user can't resume game.
    val mode = mBallsView.getMode
    val toRestore =
      if (mode == DivideAndConquerView.Mode.Paused)
                DivideAndConquerView.Mode.PausedByUser else mode
    mRestoreMode push toRestore
  }

  private def restoreMode() {
    if (!mRestoreMode.isEmpty) {
      mBallsView setMode mRestoreMode.pop()
    }
  }

  /** {@inheritDoc} */
  def onBallHitsMovingLine(ballEngine: BallEngine, x: Float, y: Float) {
    mNumLives -= 1
    if (mNumLives == 0) {
      saveMode()
      mBallsView setMode DivideAndConquerView.Mode.Paused

      // vibrate three times
      if (mVibrateOn) {
        mVibrator.vibrate(Array( 0l, COLLISION_VIBRATE_MILLIS,
                                50l, COLLISION_VIBRATE_MILLIS,
                                50l, COLLISION_VIBRATE_MILLIS),
                          -1)
      }
      showDialog(GAME_OVER_DIALOG)
    } else {
      if (mVibrateOn) {
        mVibrator vibrate COLLISION_VIBRATE_MILLIS
      }
      updateLivesDisplay(mNumLives)
      if (mNumLives <= 1)
        mBallsView.postDelayed(mOneLifeToastRunnable, 700)
      else
        mBallsView.postDelayed(mLivesBlinkRedRunnable, 700)
    }
  }

  private val mOneLifeToastRunnable = new Runnable() {
    def run() {
      showToast("1 life left!")
    }
  }

  private val mLivesBlinkRedRunnable = new Runnable() {
    def run() {
      mLivesLeft setTextColor Color.RED
      mLivesLeft.postDelayed(mLivesTextWhiteRunnable, 2000)
    }
  }

  /** {@inheritDoc} */
  def onAreaChange(ballEngine: BallEngine) {
    val percentageFilled = ballEngine.getPercentageFilled
    updatePercentDisplay(percentageFilled)
    if (percentageFilled > LEVEL_UP_THRESHOLD) {
      levelUp(ballEngine)
    }
  }

  /**
   * Go to the next level
   * @param ballEngine The ball engine.
   */
  private def levelUp(ballEngine: BallEngine) {
    mNumBalls += 1

    updatePercentDisplay(0)
    updateLevelDisplay(mNumBalls)
    ballEngine.reset(SystemClock.elapsedRealtime, mNumBalls)
    mBallsView setMode DivideAndConquerView.Mode.Bouncing
    if (mNumBalls % 4 == 0) {
      mNumLives += 1
      updateLivesDisplay(mNumLives)
      showToast("bonus life!")
    }
    if (mNumBalls == 10) {
      showToast("Level 10? You ROCK!")
    } else if (mNumBalls == 15) {
      showToast("BALLS TO THE WALL!")
    }
  }

  private val mLivesTextWhiteRunnable = new Runnable() {
    def run() {
      mLivesLeft setTextColor Color.WHITE
    }
  }

  private def showToast(text: String) {
    cancelToasts()
    mCurrentToast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    mCurrentToast.show()
  }

  private def cancelToasts() {
    if (mCurrentToast != null) {
      mCurrentToast.cancel()
      mCurrentToast = null
    }
  }

  /**
   * Update the header that displays how much of the space has been contained.
   * @param amountFilled The fraction, between 0 and 1, that is filled.
   */
  private def updatePercentDisplay(amountFilled: Float) {
    val prettyPercent = (amountFilled *100).toInt
    mPercentContained setText
                getString(R.string.percent_contained, prettyPercent.asInstanceOf[AnyRef])
  }

  /** {@inheritDoc} */
  def onNewGame() {
    mNumBalls = NEW_GAME_NUM_BALLS
    mNumLives = mNumLivesStart
    updatePercentDisplay(0)
    updateLivesDisplay(mNumLives)
    updateLevelDisplay(mNumBalls)
    mBallsView.getEngine.reset(SystemClock.elapsedRealtime, mNumBalls)
    mBallsView setMode DivideAndConquerView.Mode.Bouncing
  }

  /**
   * Update the header displaying the current level
   */
  private def updateLevelDisplay(numBalls: Int) {
    mLevelInfo setText getString(R.string.level, numBalls.asInstanceOf[AnyRef])
  }

  /**
   * Update the display showing the number of lives left.
   * @param numLives The number of lives left.
   */
  def updateLivesDisplay(numLives: Int) {
    val text =
      if (numLives == 1) getString(R.string.one_life_left)
      else getString(R.string.lives_left, numLives.asInstanceOf[AnyRef])
    mLivesLeft setText text
  }

  /** {@inheritDoc} */
  def onCancel(dialog: DialogInterface) {
    if (dialog == mWelcomeDialog || dialog == mGameOverDialog) {
      // user hit back, they're done
      finish()
    }
  }
}

object DivideAndConquerActivity {
  private final val NEW_GAME_NUM_BALLS = 1
  private final val LEVEL_UP_THRESHOLD = 0.8
  private final val COLLISION_VIBRATE_MILLIS = 50

  private final val WELCOME_DIALOG = 20
  private final val GAME_OVER_DIALOG = 21

  private final val MENU_NEW_GAME = Menu.FIRST
  private final val MENU_SETTINGS = Menu.FIRST + 1
}
