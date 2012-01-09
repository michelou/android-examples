package com.manning.aip.mymoviesdatabase.model

class ModelBase {
  protected var id: Long = _

  def getId: Long = this.id
  def setId(id: Long) { this.id = id }

  override def toString: String = "ModelBase [id=" + this.id + "]"

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (this.id ^ (this.id >>> 32)).toInt
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false

    obj match {
      case other: ModelBase => this.id == other.id
      case _ => false
    }
  }
}
