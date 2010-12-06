package com.androidbook.triviaquiz

import java.io.{BufferedReader, InputStreamReader, IOException, InputStream}

import android.os.Bundle
import android.util.Log
import android.widget.TextView

import scala.android.app.Activity

class QuizHelpActivity extends Activity {
  import QuizConstants._

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.help)

    // Read raw file into string and populate TextView
    val iFile = getResources openRawResource R.raw.quizhelp
    try {
      val helpText = findTextView(R.id.TextView_HelpText)
      val strFile = inputStreamToString(iFile)
      helpText setText strFile
    } catch {
      case e: Exception =>
        Log.e(DEBUG_TAG, "InputStreamToString failure", e)
    }
  }

  /**
   * Converts an input stream to a string
   * 
   * @param is
   *            The {@code InputStream} object to read from
   * @return A {@code String} object representing the string for of the input
   * @throws IOException
   *             Thrown on read failure from the input
   */
   @throws(classOf[Exception])
   def inputStreamToString(is: InputStream): String = {
     val sBuffer = new StringBuffer()
     val dataIO = new BufferedReader(new InputStreamReader(is))
     var strLine = dataIO.readLine()

     while (strLine != null) {
       sBuffer.append(strLine + "\n")
       strLine = dataIO.readLine()
     }

     dataIO.close()
     is.close()

     sBuffer.toString
  }
}
