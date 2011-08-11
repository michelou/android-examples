package com.manning.aip.fileexplorer

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView, Toast}

import util.FileUtil

import java.io.File

class ExternalStorage extends Activity {

  // wrap some operations that are likely to be needed in more than one place in FileUtil

  private var input: EditText = _
  private var output: TextView = _
  private var writeBtn: Button = _
  private var readBtn: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    this.setContentView(R.layout.external_storage)

    this.input = findViewById(R.id.external_storage_input).asInstanceOf[EditText]
    this.output = findViewById(R.id.external_storage_output).asInstanceOf[TextView]

    this.writeBtn = findViewById(R.id.external_storage_write_button).asInstanceOf[Button]
    this.writeBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) { write() }
    }

    this.readBtn = findViewById(R.id.external_storage_read_button).asInstanceOf[Button]
    this.readBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) { read() }
    }
  }

  private def write() {
    if (FileUtil.isExternalStorageWritable) {
      val dir = FileUtil.getExternalFilesDirAllApiLevels(this.getPackageName());
      val file = new File(dir, "test.txt")
      FileUtil.writeStringAsFile(input.getText.toString, file)
      Toast.makeText(this, "File written", Toast.LENGTH_SHORT).show()
      input setText ""
      output setText ""
    } else
      Toast.makeText(this, "External storage not writable", Toast.LENGTH_SHORT).show()
  }

  private def read() {
    if (FileUtil.isExternalStorageReadable) {
      val dir = FileUtil.getExternalFilesDirAllApiLevels(this.getPackageName)
      val file = new File(dir, "test.txt")
      if (file.exists && file.canRead) {
        output.setText(FileUtil.readFileAsString(file))
        Toast.makeText(this, "File read", Toast.LENGTH_SHORT).show()
      } else
        Toast.makeText(this, "Unable to read file: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show()
    } else
      Toast.makeText(this, "External storage not readable", Toast.LENGTH_SHORT).show()
  }
}
