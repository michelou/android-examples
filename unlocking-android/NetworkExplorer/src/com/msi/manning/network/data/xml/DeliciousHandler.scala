package com.msi.manning.network.data.xml

import org.xml.sax.{Attributes, SAXException}
import org.xml.sax.helpers.DefaultHandler

import scala.collection.mutable.ListBuffer

class DeliciousHandler extends DefaultHandler {
  import DeliciousHandler._  // companion object

  private val posts = new ListBuffer[DeliciousPost]

  @throws(classOf[SAXException])
  override def startDocument() {}

  @throws(classOf[SAXException])
  override def endDocument() {}

  @throws(classOf[SAXException])
  override def startElement(namespaceURI: String, localName: String, qName: String, atts: Attributes) {
    if (localName equals DeliciousHandler.POST) {
      val href = getAttributeValue("href", atts)
      val desc = getAttributeValue("description", atts)
      val tag = getAttributeValue("tag", atts)
      val post = DeliciousPost(href, desc, tag, "")
      posts += post
    }
  }

  @throws(classOf[SAXException])
  override def endElement(namespaceURI: String, localName: String, qName: String) {}

  override def characters(ch: Array[Char], start: Int, length: Int) {}

  private def getAttributeValue(attName: String, atts: Attributes): String = {
    var result: String = null
    var found = false
    for (i <- 0 until atts.getLength if !found) {
      val thisAtt = atts getLocalName i
      if (attName equals thisAtt) {
        result = atts getValue i
        found = true
      }
    }
    result
  }

  def getPosts: List[DeliciousPost] = posts.toList
}

object DeliciousHandler {
  private final val POST = "post"
}
