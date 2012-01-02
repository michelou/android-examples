package com.manning.aip

import android.content.Context
import android.view.{View, ViewGroup}
import android.widget.{ArrayAdapter, CheckedTextView, ImageView}

class MovieAdapter(context: Context)
extends ArrayAdapter[Movie](context, R.layout.movie_item, android.R.id.text1,
                            Movie.inflateFromXml(context)) {

  private val movieCollection = new collection.mutable.HashMap[Int, Boolean]
  private val movieIconUrls = context.getResources getStringArray R.array.movie_thumbs

  def toggleMovie(position: Int) {
    movieCollection.put(position, !isInCollection(position))
  }

  def isInCollection(position: Int): Boolean =
    movieCollection.getOrElse(position, false)

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val listItem = super.getView(position, convertView, parent)

    val checkMark = (listItem findViewById android.R.id.text1).asInstanceOf[CheckedTextView]
    checkMark setChecked isInCollection(position)

    val imageView = (listItem findViewById R.id.movie_icon).asInstanceOf[ImageView]
    imageView setImageDrawable null
    imageView setTag position
    val imageUrl = this.movieIconUrls(position)
    new DownloadTask(position, imageView).execute(imageUrl)

    listItem
  }
}
