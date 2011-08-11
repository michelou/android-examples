package com.manning.aip.fileexplorer

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView, Toast}

import util.FileUtil

import java.io.{FileInputStream, FileNotFoundException, FileOutputStream, IOException}
import java.util.Scanner

class InternalStorage extends Activity {
  import InternalStorage._  // companion object

  // no utils here, just basic java.io

  // also be aware of getCacheDir, which writes to an internal
  // directory that the system may clean up
   
  private var input: EditText = _
  private var output: TextView = _
  private var writeBtn: Button = _
  private var readBtn: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    this.setContentView(R.layout.internal_storage)

    this.input = findViewById(R.id.internal_storage_input).asInstanceOf[EditText]
    this.output = findViewById(R.id.internal_storage_output).asInstanceOf[TextView]

    this.writeBtn = findViewById(R.id.internal_storage_write_button).asInstanceOf[Button]
    this.writeBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) { write() }
    }

    this.readBtn = findViewById(R.id.internal_storage_read_button).asInstanceOf[Button]
    this.readBtn setOnClickListener new OnClickListener() {
      def onClick(v: View) { read() }
    }
  }

  private def write() {
    var fos: FileOutputStream = null
    try {
      // note that there are many modes you can use
      fos = openFileOutput("test.txt", Context.MODE_PRIVATE)
      fos.write(input.getText.toString.getBytes)
      Toast.makeText(this, "File written", Toast.LENGTH_SHORT).show()
      input setText ""
      output setText ""
    } catch {
      case e: FileNotFoundException =>
        Log.e(Constants.LOG_TAG, "File not found", e)
      case e: IOException =>
        Log.e(Constants.LOG_TAG, "IO problem", e)
    } finally {
      try fos.close()
      catch {
        case e: IOException =>
          // ignore, and take the verbosity punch from Java ;)
      }
    }
 }

  private def read() {
    var fis: FileInputStream = null
    var scanner: Scanner = null
    var sb = new StringBuilder()
    try {
      fis = openFileInput("test.txt")
      // scanner does mean one more object, but it's easier to work with
      scanner = new Scanner(fis)
      while (scanner.hasNextLine) {
        sb append scanner.nextLine()
        sb append FileUtil.LINE_SEP
      }
      Toast.makeText(this, "File read", Toast.LENGTH_SHORT).show()
    } catch {
      case e: FileNotFoundException =>
        Log.e(Constants.LOG_TAG, "File not found", e)
    } finally {
      if (fis != null) {
        try fis.close()
        catch {
          case e: IOException =>
            // ignore, and take the verbosity punch from Java ;)
        }
      }
      if (scanner != null)
        scanner.close()
    }      
    output setText sb.toString
  }
}

object InternalStorage {
  private final val LINE_SEP = System.getProperty("line.separator")
}

