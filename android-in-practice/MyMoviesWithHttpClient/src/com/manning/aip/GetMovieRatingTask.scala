package com.manning.aip

import java.io.InputStream

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet

import android.app.{Activity, Dialog}
import android.os.AsyncTask
import android.widget.{TextView, Toast}

import com.manning.aip.json.JsonMovieParser
import com.manning.aip.xml.{SAXMovieParser, XmlPullMovieParser}

class GetMovieRatingTask(activity: Activity) extends AsyncTask[/*String*/AnyRef, /*Void*/AnyRef, Movie] {
  import GetMovieRatingTask._  // companion object

  private var parserKind = PARSER_KIND_SAX

  override protected def doInBackground(params: /*String*/AnyRef*): Movie =
    try {
      val imdbId = params(0).asInstanceOf[String]
      val httpClient = MyMovies.getHttpClient
      val format = if (parserKind == PARSER_KIND_JSON) "json" else "xml"
      val path = "/Movie.imdbLookup/en/" + format + "/" + API_KEY + "/" + imdbId
      val request = new HttpGet(API_ENDPOINT + path)

      val response = httpClient execute request
      val data = response.getEntity.getContent

      parserKind match {
        case PARSER_KIND_SAX =>
          SAXMovieParser.parseMovie(data)
        case PARSER_KIND_XMLPULL =>
          XmlPullMovieParser.parseMovie(data)
        case PARSER_KIND_JSON =>
          JsonMovieParser.parseMovie(data)
        case _ =>
          throw new RuntimeException("unsupported parser")
      }
    } catch {
      case e: Exception =>
        e.printStackTrace()
        null
    }

  override protected def onPostExecute(movie: Movie) {
    if (movie == null) {
      Toast.makeText(activity, "Error!", Toast.LENGTH_SHORT).show()
      return
    }
    val dialog = new Dialog(activity)
    dialog setContentView R.layout.movie_dialog

    dialog.setTitle("IMDb rating for \"" + movie.getTitle + "\"")

    val rating = (dialog findViewById R.id.movie_dialog_rating).asInstanceOf[TextView]
    rating setText movie.getRating

    dialog.show()
  }
}

object GetMovieRatingTask {
  private final val API_KEY = "624645327f33f7866355b7b728f9cd98"

  private final val API_ENDPOINT = "http://api.themoviedb.org/2.1"

  private final val PARSER_KIND_SAX = 0
  private final val PARSER_KIND_XMLPULL = 1
  private final val PARSER_KIND_JSON = 2
}
