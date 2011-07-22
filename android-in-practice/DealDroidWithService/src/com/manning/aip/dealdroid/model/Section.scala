package com.manning.aip.dealdroid.model

case class Section(title: String) {
  private val itemList = new collection.mutable.ListBuffer[Item]

  def +=(item: Item) { itemList += item }
  def items = itemList.toList

  override def toString: String =
    if (this.title != null) this.title else "NULL"

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (this.itemList == null) 0 else this.itemList.hashCode)
    result = prime * result + (if (this.title == null) 0 else this.title.hashCode)
    result
  }

  override def equals(other: Any): Boolean = other match {
    case that: Section =>
      (this.itemList == that.itemList) && (this.title == that.title)
    case _ =>
      false
  }
}
