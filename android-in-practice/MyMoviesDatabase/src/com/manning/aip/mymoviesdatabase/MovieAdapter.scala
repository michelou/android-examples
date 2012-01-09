package com.manning.aip.mymoviesdatabase

import android.content.Context
import android.view.{View, ViewGroup}
import android.widget.{ArrayAdapter, ImageView, TextView}

import java.util.{List => JList}

import model.Movie
import util.ImageCache

class MovieAdapter(context: Context, cache: ImageCache, movies: JList[Movie])
extends ArrayAdapter[Movie](context, R.layout.movie_item, android.R.id.text1, movies) {

  override def getView(position: Int, convertView: View, parent: ViewGroup): View = {
    val listItem = super.getView(position, convertView, parent)

    var text: TextView = null
    var image: ImageView = null
    var holder = listItem.getTag.asInstanceOf[ViewHolder]
    if (holder != null) {
      text = holder.text
      image = holder.image
    } else {
      text = listItem.findViewById(android.R.id.text1).asInstanceOf[TextView]
      image = listItem.findViewById(R.id.movie_icon).asInstanceOf[ImageView]
      holder = new ViewHolder(text, image)
      listItem setTag holder
    }

    val movie: Movie = this.getItem(position)

    text setText movie.getName

    image setImageDrawable null
    image setTag position.toLong
    val thumbUrl = movie.getThumbUrl
    if ((thumbUrl != null) && !thumbUrl.equals("")) {
      if (cache.get(thumbUrl) == null)
        new DownloadListViewTask(cache, position, image) execute thumbUrl
      else
        image setImageBitmap cache.get(thumbUrl)
    }

    listItem
  }

  private case class ViewHolder(text: TextView, image: ImageView)
}
