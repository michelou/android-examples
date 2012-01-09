package com.manning.aip.mymoviesdatabase
package xml

import android.util.{Log, Xml}

import org.xmlpull.v1.XmlPullParser

import java.io.{IOException, InputStream}
import java.net.{MalformedURLException, URL, URLEncoder}

import scala.collection.mutable.ListBuffer

import model.{Category, Movie, MovieSearchResult}

class TheMovieDBXmlPullFeedParser extends MovieFeed {
  import TheMovieDBXmlPullFeedParser._  // companion object

  private val ID = "id"

  private def getSearchInputStream(name: String): InputStream = {
    var url: URL = null
    try {
      url = new URL(TheMovieDBXmlPullFeedParser.SEARCH_FEED_URL + URLEncoder.encode(name, "UFT-8"))
      Log.d(Constants.LOG_TAG, "Movie search URL: " + url)
    } catch {
      case e: MalformedURLException =>
        throw new RuntimeException(e)
    }

    try url.openConnection.getInputStream
    catch { case e: IOException => throw new RuntimeException(e) }
  }

  private def getInfoInputStream(tmdbId: String): InputStream = {
    var url: URL = null
    try {
      url = new URL(TheMovieDBXmlPullFeedParser.INFO_FEED_URL + URLEncoder.encode(tmdbId, "UTF-8"))
      Log.d(Constants.LOG_TAG, "Movie info URL: " + url)
    } catch {
      case e: MalformedURLException =>
        throw new RuntimeException(e)
    }

    try url.openConnection.getInputStream
    catch { case e: IOException => throw new RuntimeException(e) }
  }

  override def search(searchName: String): List[MovieSearchResult] = {
    val movies = new ListBuffer[MovieSearchResult]
    val parser = Xml.newPullParser()
    try {
      // auto-detect the encoding from the stream
      parser.setInput(this.getSearchInputStream(searchName), null)
      var eventType = parser.getEventType

      var movie: MovieSearchResult = null
      while (eventType != XmlPullParser.END_DOCUMENT) {
        var name: String = null
        eventType match {
          case XmlPullParser.START_DOCUMENT =>

          case XmlPullParser.START_TAG =>
            name = parser.getName
            val nextText = parser.nextText()
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.MOVIE)
              movie = new MovieSearchResult()
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.NAME)
              movie setName nextText
            if (name equalsIgnoreCase ID)
              movie setProviderId nextText
          case XmlPullParser.END_TAG =>
            name = parser.getName
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.MOVIE) {
              if (movie != null) movies += movie
            }
        }
        eventType = parser.next()
      }

    } catch {
      case e: Exception =>
        Log.e(Constants.LOG_TAG, "Exception parsing XML", e)
    }

    movies.toList
  }

  override def get(providerId: String): Movie = {
    var movie: Movie = null
    val parser = Xml.newPullParser()
    try {
      // auto-detect the encoding from the stream
      parser.setInput(this.getInfoInputStream(providerId), null)
      var eventType = parser.getEventType

      while (eventType != XmlPullParser.END_DOCUMENT) {
        var name: String = null

        eventType match {
          case XmlPullParser.START_DOCUMENT =>

          case XmlPullParser.START_TAG =>
            name = parser.getName()

            // we handle image tags, which are empty and have only attributes, before we call parser.nextText()
            // (nextX will move to the END_TAG and then ExpatParser will throw an exception 
            // (can't get attributes on END)
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.IMAGE) {
              val typ = parser.getAttributeValue(parser.getNamespace, TheMovieDBXmlPullFeedParser.TYPE)
              val size = parser.getAttributeValue(parser.getNamespace, TheMovieDBXmlPullFeedParser.SIZE)
              val url = parser.getAttributeValue(parser.getNamespace, TheMovieDBXmlPullFeedParser.URL)
              if ((typ != null) && typ.equalsIgnoreCase(TheMovieDBXmlPullFeedParser.POSTER)) {
                if ((size != null) && size.equalsIgnoreCase(TheMovieDBXmlPullFeedParser.THUMB))
                  movie setThumbUrl url
                else if ((size != null) && size.equalsIgnoreCase(TheMovieDBXmlPullFeedParser.COVER))
                  movie setImageUrl url
              }
            }

            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.CATEGORY) {
              val categoryName = parser.getAttributeValue(parser.getNamespace, TheMovieDBXmlPullFeedParser.NAME)
              val category = new Category(0, categoryName)
              if (!movie.getCategories.contains(category))
                movie.getCategories add category
            }

            val nextText = parser.nextText()
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.MOVIE)
              movie = new Movie()
            if (name equalsIgnoreCase ID)
              movie setProviderId nextText
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.HOMEPAGE)
              movie setHomepage nextText
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.NAME)
              movie setName nextText
            if (name.equalsIgnoreCase(TheMovieDBXmlPullFeedParser.RATING)) {
              if ((nextText != null) && !nextText.equals("") && !nextText.equals("0"))
                movie setRating nextText.toDouble
            }
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.TAGLINE)
              movie setTagline nextText
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.TRAILER)
              movie setTrailer nextText
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.URL)
              movie setUrl nextText
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.RELEASED)
              if ((nextText != null) && !nextText.equals("") && !nextText.equals("0")) {
                val yearString = nextText.substring(0, 4)
                movie setYear yearString.toInt
              }
          case XmlPullParser.END_TAG =>
            name = parser.getName
            if (name equalsIgnoreCase TheMovieDBXmlPullFeedParser.MOVIE)
              return movie
        }
        eventType = parser.next()
      }

    } catch {
      case e: Exception =>
        Log.e(Constants.LOG_TAG, "Exception parsing XML", e)
    }
    movie
  }
}

object TheMovieDBXmlPullFeedParser {
  // to use TheMovieDb.org feed you need an API key, get your own
  // (this one was created for the book and won't be around forever)
  // http://api.themoviedb.org/
  private final val API_KEY = "e83a393a2cd8bf5a978ee1909e32d531"

  // feed urls
  private final val SEARCH_FEED_URL =
    "http://api.themoviedb.org/2.1/Movie.search/en/xml/" + TheMovieDBXmlPullFeedParser.API_KEY + "/"
  private final val INFO_FEED_URL =
    "http://api.themoviedb.org/2.1/Movie.getInfo/en/xml/" + TheMovieDBXmlPullFeedParser.API_KEY + "/"

  // names of the XML tags   
  private final val MOVIE = "movie"
  private final val NAME = "name"
  private final val RELEASED = "released"
  private final val RATING = "rating"
  private final val TAGLINE = "tagline"
  private final val TRAILER = "trailer"
  private final val URL = "url"
  private final val HOMEPAGE = "homepage"
  private final val IMAGE = "image"
  private final val THUMB = "thumb"
  private final val COVER = "cover"
  private final val POSTER = "poster"
  private final val CATEGORY = "category"
  private final val TYPE = "type"
  private final val SIZE = "size"
}
