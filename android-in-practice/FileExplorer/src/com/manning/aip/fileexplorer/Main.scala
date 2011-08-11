package com.manning.aip.fileexplorer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

class Main extends Activity with OnClickListener {

  private var internalStorage: Button = _
  private var externalStorage: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    this.setContentView(R.layout.main)

    this.internalStorage = findViewById(R.id.main_internal_storage_button).asInstanceOf[Button]
    this.internalStorage setOnClickListener this

    this.externalStorage = findViewById(R.id.main_external_storage_button).asInstanceOf[Button]
    this.externalStorage setOnClickListener this
  }

  def onClick(v: View) {
    if (v equals internalStorage)
      startActivity(new Intent(this, classOf[InternalStorage]))
    else if (v equals externalStorage)
      startActivity(new Intent(this, classOf[ExternalStorage]))
  }
}

