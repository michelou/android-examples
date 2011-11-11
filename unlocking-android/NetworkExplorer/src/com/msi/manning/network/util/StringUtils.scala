package com.msi.manning.network.util

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}

object StringUtils {

  @throws(classOf[IOException])
  def inputStreamToString(stream: InputStream): String = {
    val br = new BufferedReader(new InputStreamReader(stream))
    val sb = new StringBuilder()
    var line = br.readLine()
    while (line != null) {
      sb.append(line + "\n")
      line = br.readLine()
    }
    br.close()
    sb.toString()
  }

}
