package com.manning.aip
package xml

import java.io.InputStream

import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

import android.util.Xml
import android.util.Xml.Encoding

class SAXMovieParser extends DefaultHandler {
  private var movie: Movie = _
  private var elementText: StringBuilder = _

  def getMovie: Movie = movie

  @throws(classOf[SAXException])
  override def startDocument() {
    elementText = new StringBuilder()
  }

  @throws(classOf[SAXException])
  override def startElement(uri: String, localName: String, qName: String,
            attributes: Attributes) {
    if ("movie" equals localName) {
      movie = new Movie()
    }
  }

  @throws(classOf[SAXException])
  override def endElement(uri: String, localName: String, qName: String) {
    if ("name" equals localName)
      movie setTitle elementText.toString.trim()
    else if ("rating" equals localName)
      movie setRating elementText.toString.trim()
    elementText setLength 0
  }

  @throws(classOf[SAXException])
  override def characters(ch: Array[Char], start: Int, length: Int) {
    elementText.appendAll(ch, start, length)
  }
}

object SAXMovieParser {
  @throws(classOf[Exception])
  def parseMovie(xml: InputStream): Movie = {
    val parser = new SAXMovieParser()
    Xml.parse(xml, Encoding.UTF_8, parser)
    parser.getMovie
  }
}
