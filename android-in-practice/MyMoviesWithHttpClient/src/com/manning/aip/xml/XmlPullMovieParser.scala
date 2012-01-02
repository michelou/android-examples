package com.manning.aip
package xml

import java.io.InputStream

import org.xmlpull.v1.{XmlPullParser, XmlPullParserFactory}

class XmlPullMovieParser {
  private var xpp: XmlPullParser = _

  @throws(classOf[Exception])
  def parse(xml: InputStream): Movie = {
    val movie = new Movie()

    xpp = XmlPullParserFactory.newInstance().newPullParser()
    xpp.setInput(xml, "UTF-8")

    skipToTag("name")
    movie setTitle xpp.nextText()

    skipToTag("rating")
    movie setRating xpp.nextText()

    movie
  }

  @throws(classOf[Exception])
  private def skipToTag(tagName: String) {
    var event = xpp.getEventType
    while (event != XmlPullParser.END_DOCUMENT && !tagName.equals(xpp.getName)) {
      event = xpp.next()
    }
  }
}

object XmlPullMovieParser {
  @throws(classOf[Exception])
  def parseMovie(xml: InputStream): Movie = new XmlPullMovieParser().parse(xml)
}
