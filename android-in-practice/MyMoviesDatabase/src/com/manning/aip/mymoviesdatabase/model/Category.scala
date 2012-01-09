package com.manning.aip.mymoviesdatabase.model

class Category extends ModelBase with Comparable[Category] {

  private var name: String = _

  // NOTE in real-world android app you might want a CategoryFactory
  // or factory method here, to cut down on number of objects created
  // (there will only ever be a small number of categories, and they are simple, 
  // no need to recreate objects over again that represent same String)  

  def this(id: Long, name: String) {
    this()
    this.id = id
    this.name = name
  }

  def getName: String = this.name
  def setName(name: String) { this.name = name }

  override def toString: String = this.name

  override def compareTo(another: Category): Int =
    if (another == null) -1
    else if (this.name == null) 1
    else this.name.compareTo(another.name)

  override def hashCode: Int = {
    val prime = 31
    var result = super.hashCode
    // upper name so hashCode is consistent with equals (equals ignores case)
    result = prime * result + (if (this.name == null) 0 else this.name.toUpperCase.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (!super.equals(obj)) return false
    
    obj match {
      case other: Category =>
        (this.name != null) && (this.name.equalsIgnoreCase(other.name)) ||
        (other.name == null)
      case _ =>
        false
    }
  }
}
