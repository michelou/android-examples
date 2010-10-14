/***
 * Excerpted from "Hello, Android! 3rd",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.audio

import android.app.Activity
import android.media.{AudioManager, MediaPlayer}
import android.os.Bundle
import android.view.KeyEvent

class Audio extends Activity {
  private var mp: MediaPlayer = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)
    setVolumeControlStream(AudioManager.STREAM_MUSIC)
  }

  override def onKeyDown(keyCode: Int, event: KeyEvent): Boolean = {
    val resId = keyCode match {
      case KeyEvent.KEYCODE_DPAD_UP    => R.raw.up
      case KeyEvent.KEYCODE_DPAD_DOWN  => R.raw.down
      case KeyEvent.KEYCODE_DPAD_LEFT  => R.raw.left
      case KeyEvent.KEYCODE_DPAD_RIGHT => R.raw.right
      case KeyEvent.KEYCODE_DPAD_CENTER |
           KeyEvent.KEYCODE_ENTER      => R.raw.enter
      case KeyEvent.KEYCODE_A          => R.raw.a
      case KeyEvent.KEYCODE_S          => R.raw.s
      case KeyEvent.KEYCODE_D          => R.raw.d
      case KeyEvent.KEYCODE_F          => R.raw.f
      case _ =>
        return super.onKeyDown(keyCode, event)
    }

    // Release any resources from previous MediaPlayer
    if (mp != null) mp.release()

    // Create a new MediaPlayer to play this sound
    mp = MediaPlayer.create(this, resId)
    mp.start()

    // Indicate this key was handled
    true
  }
}

