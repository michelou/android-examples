package com.msi.manning.network

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{Button, EditText, TextView}

import java.io.{BufferedReader, BufferedWriter, IOException, InputStreamReader, OutputStreamWriter}
import java.net.Socket

/**
 * Android direct to Socket example.
 * 
 * For this to work you need a server listening on the IP address and port
 * specified. See the NetworkSocketServer project for an example.
 * 
 * 
 * @author charliecollins
 * 
 */
class SimpleSocket extends Activity {
  import SimpleSocket._  // companion object

  private var ipAddress: EditText = _
  private var port: EditText = _
  private var socketInput: EditText = _
  private var socketOutput: TextView = _
  private var socketButton: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.simple_socket)

    ipAddress = findViewById(R.id.socket_ip).asInstanceOf[EditText]
    port = findViewById(R.id.socket_port).asInstanceOf[EditText]
    socketInput = findViewById(R.id.socket_input).asInstanceOf[EditText]
    socketOutput = findViewById(R.id.socket_output).asInstanceOf[TextView]
    socketButton = findViewById(R.id.socket_button).asInstanceOf[Button]

    socketButton setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        socketOutput setText ""
        val output = callSocket(ipAddress.getText.toString, port.getText.toString, socketInput.getText.toString)
        socketOutput setText output
      }
    }
  }

  private def callSocket(ip: String, port: String, socketData: String): String = {
    var socket: Socket = null
    var writer: BufferedWriter = null
    var reader: BufferedReader = null
    var output: String = null

    try {
      socket = new Socket(ip, port.toInt)
      writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream))
      reader = new BufferedReader(new InputStreamReader(socket.getInputStream))

      // send input terminated with \n
      val input = socketData;
      writer.write(input + "\n", 0, input.length() + 1)
      writer.flush()

      // read back output
      output = reader.readLine()
      Log.d(Constants.LOGTAG, " " + CLASSTAG + " output - " + output)

      // send EXIT and close
      writer.write("EXIT\n", 0, 5)
      writer.flush()

    } catch {
      case e: IOException =>
        Log.e(Constants.LOGTAG, " " + CLASSTAG + " IOException calling socket", e)
    } finally {
      try if (writer != null) writer.close()
      catch { case e: IOException => /* swallow */ }
      try if (reader != null) reader.close()
      catch { case e: IOException => /* swallow */ }
      try if (socket != null) socket.close()
      catch { case e: IOException => /* swallow */ }
    }
        
    output
  }
}

object SimpleSocket {
  private final val CLASSTAG = classOf[SimpleSocket].getSimpleName
}

