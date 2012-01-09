package com.manning.aip.mymoviesdatabase.model

import scala.collection.mutable.{Set, LinkedHashSet}

class Movie extends ModelBase {

  private var providerId: String = _
  private var name: String = _
  private var year: Int = _
  private var rating: Double = _
  private var url: String = _
  private var homepage: String = _
  private var trailer: String = _
  private var tagline: String = _
  private var thumbUrl: String = _
  private var imageUrl: String = _
  private var categories: Set[Category] = new LinkedHashSet()

  // note, in the real-world making these model beans immutable would be a better approach
  // (that is to say, not making them JavaBeans, but making immutable model classes with Builder)

  def getProviderId: String = this.providerId
  def setProviderId(providerId: String) { this.providerId = providerId }

  def getName: String = this.name
  def setName(name: String) { this.name = name }

  def getYear: Int = this.year
  def setYear(year: Int) { this.year = year }

  def getRating: Double = this.rating
  def setRating(rating: Double) { this.rating = rating }

  def getUrl: String = this.url
  def setUrl(url: String) { this.url = url }

  def getHomepage: String = this.homepage
  def setHomepage(homepage: String) { this.homepage = homepage }

  def getTrailer: String = this.trailer
  def setTrailer(trailer: String) { this.trailer = trailer }

  def getTagline: String = this.tagline
  def setTagline(tagline: String) { this.tagline = tagline }

  def getThumbUrl: String = this.thumbUrl
  def setThumbUrl(thumbUrl: String) { this.thumbUrl = thumbUrl }

  def getImageUrl: String = this.imageUrl
  def setImageUrl(imageUrl: String) { this.imageUrl = imageUrl }

  def getCategories: Set[Category] = this.categories
  def setCategories(categories: Set[Category]) { this.categories = categories }

  override def toString: String =
    "Movie [categories=" + this.categories + ", homepage=" + this.homepage +
    ", name=" + this.name + ", providerId=" + this.providerId +
    ", rating=" + this.rating + ", tagline=" + this.tagline +
    ", thumbUrl=" + this.thumbUrl + ", imageUrl=" + this.imageUrl +
    ", trailer=" + this.trailer + ", url=" + this.url + ", year=" + this.year + "]"

  private def doubleToLongBits(d: Double): Long =
    java.lang.Double.doubleToLongBits(d)

  override def hashCode: Int = {
    val prime = 31
    var result = super.hashCode
    result = prime * result + (if (this.categories == null) 0 else this.categories.hashCode())
    result = prime * result + (if (this.homepage == null) 0 else this.homepage.hashCode())
    // upper name so hashCode is consistent with equals (equals ignores case)
    result = prime * result + (if (this.name == null) 0 else this.name.toUpperCase.hashCode())
    result = prime * result + (if (this.providerId == null) 0 else this.providerId.hashCode())
    val temp = doubleToLongBits(this.rating)
    result = prime * result + (temp ^ (temp >>> 32)).toInt
    result = prime * result + (if (this.tagline == null) 0 else this.tagline.hashCode)
    result = prime * result + (if (this.thumbUrl == null) 0 else this.thumbUrl.hashCode)
    result = prime * result + (if (this.imageUrl == null) 0 else this.imageUrl.hashCode)
    result = prime * result + (if (this.trailer == null) 0 else this.trailer.hashCode)
    result = prime * result + (if (this.url == null) 0 else this.url.hashCode)
    result = prime * result + this.year
    result
  }

  override def equals(obj: Any): Boolean = {
    if (this == obj) return true
    if (!super.equals(obj)) return false
    
    obj match {
      case other: Movie =>
        if (this.categories == null) {
          if (other.categories != null) return false
        } else if (!this.categories.equals(other.categories)) {
          return false
        }
        if (this.homepage == null) {
          if (other.homepage != null) return false
        } else if (!this.homepage.equals(other.homepage)) {
          return false
        }
        if (this.name == null) {
          if (other.name != null) return false
        } else if (!this.name.equalsIgnoreCase(other.name)) {
           return false
        }
        if (this.providerId == null) {
           if (other.providerId != null) return false
        } else if (!this.providerId.equals(other.providerId)) {
          return false
        }
        if (doubleToLongBits(this.rating) != doubleToLongBits(other.rating)) {
          return false
        }
        if (this.tagline == null) {
          if (other.tagline != null) return false
        } else if (!this.tagline.equals(other.tagline)) {
          return false;
        }
        if (this.thumbUrl == null) {
          if (other.thumbUrl != null) return false
        } else if (!this.thumbUrl.equals(other.thumbUrl)) {
          return false
        }
        if (this.imageUrl == null) {
          if (other.imageUrl != null) return false
        } else if (!this.imageUrl.equals(other.imageUrl)) {
          return false
        }
        if (this.trailer == null) {
          if (other.trailer != null) return false
        } else if (!this.trailer.equals(other.trailer)) {
          return false
        }
        if (this.url == null) {
          if (other.url != null) return false
        } else if (!this.url.equals(other.url)) {
          return false
        }
        this.year == other.year
      case _ =>
        false
    }
  }

}
