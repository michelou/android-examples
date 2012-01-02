package com.manning.aip

class Movie {
  private var id: String = _
  private var title: String = _
  private var rating: String = _

  override def toString: String = title

  def getId: String = id
  def setId(id: String) { this.id = id }

  def getTitle: String = title
  def setTitle(title: String) { this.title = title }

  def getRating: String = rating
  def setRating(rating: String) { this.rating = rating }
}

object Movie {
  import java.util.{List => JList}
  import android.content.Context
  import com.manning.aip.R

  def inflateFromXml(context: Context): JList[Movie] = {
    val ids = context.getResources getStringArray R.array.movie_ids
    val titles = context.getResources getStringArray R.array.movies

    val movies = for (i <- 0 until ids.length) yield {
      val movie = new Movie()
      movie setId ids(i)
      movie setTitle titles(i)
      movie
    }
    import scala.collection.JavaConversions._
    movies
  }
}
