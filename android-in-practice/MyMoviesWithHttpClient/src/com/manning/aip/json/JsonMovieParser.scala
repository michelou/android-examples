package com.manning.aip
package json

import java.io.{BufferedReader, IOException, InputStream, InputStreamReader}

import org.json.{JSONArray, JSONObject}

object JsonMovieParser {

  @throws(classOf[Exception])
  def parseMovie(json: InputStream): Movie = {
    val reader = new BufferedReader(new InputStreamReader(json))
    val sb = new StringBuilder()

    try {
      var line = reader.readLine()
      while (line != null) {
        sb append line
        line = reader.readLine()
      }
    } catch {
      case e: IOException => throw e
    } finally {
      reader.close()
    }
    val jsonReply = new JSONArray(sb.toString)

    val movie = new Movie()
    val jsonMovie = jsonReply.getJSONObject(0)
    movie setTitle (jsonMovie getString "name")
    movie setRating (jsonMovie getString "rating")

    movie
  }
}
