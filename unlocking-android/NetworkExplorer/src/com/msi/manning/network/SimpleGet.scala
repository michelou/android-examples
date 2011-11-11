package com.msi.manning.network

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView}

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.{HttpURLConnection, MalformedURLException, URL}

class SimpleGet extends Activity {
  import SimpleGet._  // companion object

  private var getInput: EditText = _
  private var getOutput: TextView = _
  private var getButton: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.simple_get)

    getInput = findViewById(R.id.get_input).asInstanceOf[EditText]
    getOutput = findViewById(R.id.get_output).asInstanceOf[TextView]
    getButton = findViewById(R.id.get_button).asInstanceOf[Button]

    getButton setOnClickListener new OnClickListener() {
       def onClick(v: View) {
         getOutput setText ""
         val output = getHttpResponse(getInput.getText.toString)
         if (output != null) {
           getOutput setText output
         }
       }
    }
  }

  /**
   * Perform an HTTP GET with HttpUrlConnection.
   * 
   * @param location
   @return
     */
  private def getHttpResponse(location: String): String = {
    var result: String = null
    var url: URL = null
    Log.d(Constants.LOGTAG, " " + CLASSTAG + " location = " + location)

    try {
      url = new URL(location)
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " url = " + url)
    } catch {
      case e: MalformedURLException =>
        Log.e(Constants.LOGTAG, " " + CLASSTAG + " " + e.getMessage)
    }

    if (url != null) {
      try {
        val urlConn = url.openConnection().asInstanceOf[HttpURLConnection]
        val in = new BufferedReader(new InputStreamReader(urlConn.getInputStream))

        var lineCount = 0; // limit the lines for the example
        var inputLine = in.readLine()
        while (lineCount < 10 && inputLine != null) {
          Log.v(Constants.LOGTAG, " " + CLASSTAG + " inputLine = " + inputLine)
          result += "\n" + inputLine
          lineCount += 1
          inputLine = in.readLine()
        }

        in.close()
        urlConn.disconnect()

      } catch {
        case e: IOException =>
          Log.e(Constants.LOGTAG, " " + CLASSTAG + " " + e.getMessage)
      }
    } else {
      Log.e(Constants.LOGTAG, " " + CLASSTAG + " url NULL")
    }
    result
  }
}

object SimpleGet {
  private final val CLASSTAG = classOf[SimpleGet].getSimpleName
}
