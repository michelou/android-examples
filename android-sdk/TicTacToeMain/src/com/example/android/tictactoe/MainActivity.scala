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

package com.example.android.tictactoe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener

import com.example.android.tictactoe.library.GameActivity
import com.example.android.tictactoe.library.GameView.State

class MainActivity extends Activity {
  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    findViewById(R.id.start_player) setOnClickListener
      new OnClickListener() {
        def onClick(v: View) {
          startGame(true)
        }
      }

    findViewById(R.id.start_comp) setOnClickListener
      new OnClickListener() {
        def onClick(v: View) {
          startGame(false)
        }
      }
  }

  private def startGame(startWithHuman: Boolean) {
    val i = new Intent(this, classOf[GameActivity])
    val player = if (startWithHuman) State.PLAYER1 else State.PLAYER2
    i.putExtra(GameActivity.EXTRA_START_PLAYER, player.id)
    startActivity(i)
  }
}
