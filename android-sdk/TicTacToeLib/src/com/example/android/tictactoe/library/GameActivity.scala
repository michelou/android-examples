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

package com.example.android.tictactoe.library

import scala.util.Random

import android.app.Activity
import android.os.{Bundle, Handler, Message}
import android.os.Handler.Callback
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, TextView}

import com.example.android.tictactoe.library.GameView.ICellListener
import com.example.android.tictactoe.library.GameView.State

class GameActivity extends Activity {
  import GameActivity._  // companion object

  private val mHandler = new Handler(new MyHandlerCallback())
  private val mRnd = new Random()
  private var mGameView: GameView = _
  private var mInfoView: TextView = _
  private var mButtonNext: Button = _

  /** Called when the activity is first created. */
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    /*
     * IMPORTANT: all resource IDs from this library will eventually be merged
     * with the resources from the main project that will use the library.
     *
     * If the main project and the libraries define the same resource IDs,
     * the application project will always have priority and override library
     * resources and IDs defined in multiple libraries are resolved based on
     * the libraries priority defined in the main project.
     *
     * An intentional consequence is that the main project can override some
     * resources from the library.
     * (TODO insert example).
     *
     * To avoid potential conflicts, it is suggested to add a prefix to the
     * library resource names.
     */
    setContentView(R.layout.lib_game)

    mGameView = findViewById(R.id.game_view).asInstanceOf[GameView]
    mInfoView = findViewById(R.id.info_turn).asInstanceOf[TextView]
    mButtonNext = findViewById(R.id.next_turn).asInstanceOf[Button]

    mGameView setFocusable true
    mGameView setFocusableInTouchMode true
    mGameView setCellListener new MyCellListener()

    mButtonNext setOnClickListener new MyButtonListener()
  }

  override protected def onResume() {
    super.onResume()

    var player = mGameView.getCurrentPlayer
    if (player == State.UNKNOWN) {
      player = State.fromInt(getIntent.getIntExtra(EXTRA_START_PLAYER, 1))
      if (!checkGameFinished(player)) {
        selectTurn(player)
      }
    }
    if (player == State.PLAYER2) {
      mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS)
    }
    if (player == State.WIN) {
      setWinState(mGameView.getWinner)
    }
  }

  private def selectTurn(player: State): State = {
    mGameView setCurrentPlayer player
    mButtonNext setEnabled false

    if (player == State.PLAYER1) {
      mInfoView setText R.string.player1_turn
      mGameView setEnabled true
    } else if (player == State.PLAYER2) {
      mInfoView setText R.string.player2_turn
      mGameView setEnabled false
    }
    player
  }

  private class MyCellListener extends GameView.ICellListener {
    def onCellSelected() {
      if (mGameView.getCurrentPlayer == State.PLAYER1) {
        val cell = mGameView.getSelection
        mButtonNext.setEnabled(cell >= 0)
      }
    }
  }

  private class MyButtonListener extends OnClickListener {
    def onClick(v: View) {
      val player = mGameView.getCurrentPlayer

      if (player == State.WIN) {
        GameActivity.this.finish()
      } else if (player == State.PLAYER1) {
        val cell = mGameView.getSelection
        if (cell >= 0) {
          mGameView.stopBlink()
          mGameView.setCell(cell, player)
          finishTurn()
        }
      }
    }
  }

  private class MyHandlerCallback extends Callback {
    def handleMessage(msg: Message): Boolean = {
      if (msg.what == MSG_COMPUTER_TURN) {

        // Pick a non-used cell at random. That's about all the AI you need
        // for this game.
        val data = mGameView.getData
        var used = 0
        while (used != 0x1F) {
          val index = mRnd nextInt 9
          if (((used >> index) & 1) == 0) {
            used |= 1 << index
            if (data(index) == State.EMPTY) {
              mGameView.setCell(index, mGameView.getCurrentPlayer)
              used = 0x1F //break
            }
          }
        }
        finishTurn()
        true
      } else
        false
    }
  }

  private def getOtherPlayer(player: State): State = 
    if (player == State.PLAYER1) State.PLAYER2 else State.PLAYER1

  private def finishTurn() {
    var player = mGameView.getCurrentPlayer
    if (!checkGameFinished(player)) {
      player = selectTurn(getOtherPlayer(player))
      if (player == State.PLAYER2) {
        mHandler.sendEmptyMessageDelayed(MSG_COMPUTER_TURN, COMPUTER_DELAY_MS)
      }
    }
  }

  def checkGameFinished(player: State): Boolean = {
    val data = mGameView.getData
    var full = true

    var col = -1
    var row = -1
    var diag = -1

    // check rows
    var k = 0
    for (j <- 0 until 3) {
      if (data(k) != State.EMPTY && data(k) == data(k+1) && data(k) == data(k+2)) {
        row = j
      }
      if (full && (data(k) == State.EMPTY ||
                   data(k+1) == State.EMPTY ||
                   data(k+2) == State.EMPTY)) {
        full = false
      }
      k += 3
    } //for

    // check columns
    for (i <- 0 until 3) {
      if (data(i) != State.EMPTY && data(i) == data(i+3) && data(i) == data(i+6)) {
        col = i
      }
    }

    // check diagonals
    if (data(0) != State.EMPTY && data(0) == data(1+3) && data(0) == data(2+6)) {
      diag = 0
    } else  if (data(2) != State.EMPTY && data(2) == data(1+3) && data(2) == data(0+6)) {
      diag = 1
    }

    if (col != -1 || row != -1 || diag != -1) {
      setFinished(player, col, row, diag)
      return true
    }

    // if we get here, there's no winner but the board is full.
    if (full) {
      setFinished(State.EMPTY, -1, -1, -1)
      true
    } else
      false
  }

  private def setFinished(player: State, col: Int, row: Int, diagonal: Int) {
    mGameView setCurrentPlayer State.WIN
    mGameView setWinner player
    mGameView setEnabled false
    mGameView.setFinished(col, row, diagonal)

    setWinState(player)
  }

  private def setWinState(player: State) {
    mButtonNext setEnabled true
    mButtonNext setText "Back"

    val id = player match {
      case State.EMPTY   => R.string.tie
      case State.PLAYER1 => R.string.player1_win
      case _             => R.string.player2_win
    }
    mInfoView setText getString(id)
  }

}

object GameActivity {
  /** Start player. Must be 1 or 2. Default is 1. */
  final val EXTRA_START_PLAYER =
    "com.example.android.tictactoe.library.GameActivity.EXTRA_START_PLAYER"

  private final val MSG_COMPUTER_TURN = 1
  private final val COMPUTER_DELAY_MS = 500l
}
