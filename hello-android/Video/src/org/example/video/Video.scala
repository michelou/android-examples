/***
 * Excerpted from "Hello, Android! 2e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package org.example.video;

import android.app.Activity
import android.os.Bundle
import android.widget.VideoView

class Video extends Activity {
  
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // Fill view from resource
    setContentView(R.layout.main)
    val video = findViewById(R.id.video).asInstanceOf[VideoView]

    // Load and start the movie
    video setVideoPath "/sdcard/samplevideo.3gp"
    video.start()
  }
}
