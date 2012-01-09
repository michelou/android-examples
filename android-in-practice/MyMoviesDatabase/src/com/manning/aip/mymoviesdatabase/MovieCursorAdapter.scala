package com.manning.aip.mymoviesdatabase

import android.content.Context
import android.database.Cursor
import android.view.{LayoutInflater, View, ViewGroup}
import android.widget.{CursorAdapter, ImageView, TextView}

import util.ImageCache

class MovieCursorAdapter(context: Context, cache: ImageCache, c: Cursor)
extends CursorAdapter(context, c, true) {

  private val vi: LayoutInflater =
    context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  override def bindView(v: View, context: Context, c: Cursor) {
    populateView(v, c)
  }

  override def newView(context: Context, c: Cursor, parent: ViewGroup): View = {
    val listItem: View = vi.inflate(R.layout.movie_item, parent, false)
    val text = listItem.findViewById(android.R.id.text1).asInstanceOf[TextView]
    val image = listItem.findViewById(R.id.movie_icon).asInstanceOf[ImageView]
    val holder = new ViewHolder(text, image)
    listItem setTag holder
    populateView(listItem, c)
    listItem
  }

  private def populateView(listItem: View, c: Cursor) {
    val holder = listItem.getTag.asInstanceOf[ViewHolder]
    if ((c != null) && !c.isClosed) {
      val id = c getLong 0
      val name = c getString 1
      val thumbUrl = c getString 2

      holder.text setText name

      holder.image setImageDrawable null
      holder.image setTag id
      if ((thumbUrl != null) && !thumbUrl.equals("")) {
        if (cache.get(thumbUrl) == null)
          new DownloadListViewTask(cache, id, holder.image) execute thumbUrl
        else
          holder.image setImageBitmap (cache get thumbUrl)
      }
    }
  }

  private case class ViewHolder(text: TextView, image: ImageView)
}

