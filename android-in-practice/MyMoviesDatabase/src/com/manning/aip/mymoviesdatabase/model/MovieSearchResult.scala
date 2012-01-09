package com.manning.aip.mymoviesdatabase.model

class MovieSearchResult {
  private var name: String = _
  private var providerId: String = _

  def getName: String = this.name
  def setName(name: String) { this.name = name }

  def getProviderId: String = this.providerId
  def setProviderId(providerId: String) { this.providerId = providerId }

  override def toString: String = name

  override def hashCode: Int = {
    val prime = 31
    var result = 1
    result = prime * result + (if (this.name == null) 0 else this.name.hashCode)
    result = prime * result + (if (this.providerId == null) 0 else this.providerId.hashCode)
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (obj == null) return false

    obj match {
      case other: MovieSearchResult =>
        if (this.name == null) {
          if (other.name != null) return false
        } else if (!this.name.equals(other.name)) {
          return false
        }
        if (this.providerId == null) {
          if (other.providerId != null) return false
        } else if (!this.providerId.equals(other.providerId)) {
          return false
        }
        true

      case _ =>
        false
    }
  }
}
