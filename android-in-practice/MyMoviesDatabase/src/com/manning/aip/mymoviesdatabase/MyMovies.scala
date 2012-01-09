package com.manning.aip.mymoviesdatabase

import android.app.{AlertDialog, ListActivity}
import android.content.{DialogInterface, Intent}
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.util.Linkify
import android.view.{ContextMenu, Menu, MenuItem, View}
import android.view.ContextMenu.ContextMenuInfo
import android.widget.AdapterView.AdapterContextMenuInfo
import android.widget.{Button, ListView, TextView}

import java.util.{ArrayList => JArrayList, List => JList}

import scala.collection.JavaConversions._

import model.Movie

class MyMovies extends ListActivity {
  import MyMovies._  // companion object

  private var aboutString: SpannableString = _
  private var app: MyMoviesApp = _

  // uncomment all lines with /// in front and comment all lines with /// at end, to use CursorAdapter
  ///private Cursor cursor;
  ///private var adapter: MovieCursorAdapter = _
   
  private var adapter: MovieAdapter = _ ///
  private var movies: JList[Movie] = _ ///

  private var backToTop: Button = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    app = getApplication.asInstanceOf[MyMoviesApp]

    val listView = getListView

    backToTop = getLayoutInflater.inflate(R.layout.list_footer, null).asInstanceOf[Button]
    backToTop.setCompoundDrawablesWithIntrinsicBounds(
      getResources getDrawable android.R.drawable.ic_menu_upload,
      null, null, null)
    // must add to ListView BEFORE setting adapter
    listView.addFooterView(backToTop, null, true)

    ///cursor = app.getDataManager.getMovieCursor
    ///if (cursor != null) {
    ///  startManagingCursor(cursor)
    ///  adapter = new MovieCursorAdapter(this, app.getImageCache, cursor)
    ///  listView setAdapter adapter
    ///}

    movies = new JArrayList[Movie]() ///
    adapter = new MovieAdapter(this, app.getImageCache, movies) ///

    listView setAdapter this.adapter
    listView setItemsCanFocus false
    listView setEmptyView findViewById(R.id.main_list_empty)
    registerForContextMenu(listView)
    aboutString = new SpannableString(MyMovies.ABOUT)
    Linkify.addLinks(aboutString, Linkify.ALL)
  }

  /// comment out entire onResume to use CursorAdapter
  override protected def onResume() {
    super.onResume()
    movies.clear()
    movies addAll app.getDataManager.getMovieHeaders
    adapter.notifyDataSetChanged()
    backToTop.setVisibility(if (movies.size() < 8) View.INVISIBLE else View.VISIBLE)
  }

  // android:onClick in layout points here
  def backToTop(view: View) { getListView setSelection 0 }

  // TODO make sure text has bugfix that was made here (lines 111-112)
  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    ///cursor moveToPosition position
    ///long movieId = cursor getInt (cursor getColumnIndex "_id")
    val intent = new Intent(this, classOf[MovieDetail])
    ///intent.putExtra(MovieDetail.MOVIE_ID_KEY, movieId)
    val movieId = l.getItemAtPosition(position).asInstanceOf[Movie].getId ///
    intent.putExtra(MovieDetail.MOVIE_ID_KEY, movieId) ///
    startActivity(intent)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, MyMovies.OPTIONS_MENU_SEARCH, 0, "Search") setIcon android.R.drawable.ic_menu_search
    menu.add(0, MyMovies.OPTIONS_MENU_CATMGR, 0, "Category Manager") setIcon android.R.drawable.ic_menu_manage
    menu.add(0, MyMovies.OPTIONS_MENU_PREFS, 0, "Preferences") setIcon android.R.drawable.ic_menu_preferences
    menu.add(0, MyMovies.OPTIONS_MENU_ABOUT, 0, "About") setIcon android.R.drawable.ic_menu_info_details
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case OPTIONS_MENU_SEARCH =>
        startActivity(new Intent(this, classOf[MovieSearch]))
      case OPTIONS_MENU_CATMGR =>
        startActivity(new Intent(this, classOf[CategoryManager]))
      case OPTIONS_MENU_PREFS =>
        startActivity(new Intent(this, classOf[Preferences]))
      case OPTIONS_MENU_ABOUT =>
        val dialog = new AlertDialog.Builder(MyMovies.this)
          .setTitle("About MyMovies")
          .setMessage(aboutString)
          .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
               def onClick(d: DialogInterface, i: Int) {}
             }).create()
        dialog.show();
        // make the Linkify'ed aboutString clickable
        dialog.findViewById(android.R.id.message).asInstanceOf[TextView]
          .setMovementMethod(LinkMovementMethod.getInstance)
    }
    false
  }

  override def onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo)
    menu.add(0, MyMovies.CONTEXT_MENU_DELETE, 0, "Delete Movie")
    menu setHeaderTitle "Action"
  }

  override def onContextItemSelected(item: MenuItem): Boolean = {
    val info = item.getMenuInfo.asInstanceOf[AdapterContextMenuInfo]
      
    ///cursor moveToPosition info.position
    ///val movieId = cursor getInt (cursor getColumnIndex "_id")
    ///val movie = app.getDataManager getMovie movieId
      
    val movieId = (movies get info.position).getId  ///
    val movie = movies get info.position ///
      
    item.getItemId match {
      case CONTEXT_MENU_DELETE =>
        new AlertDialog.Builder(MyMovies.this)
          .setTitle("Delete Movie?")
          .setMessage(movie.getName)
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
              def onClick(d: DialogInterface, i: Int) {
                app.getDataManager deleteMovie movieId
              }
            })
          .setNegativeButton("No", new DialogInterface.OnClickListener() {
              def onClick(d: DialogInterface, i: Int) {}
            }).show()
        true
      case _ =>
        super.onContextItemSelected(item)
    }
  }
}

object MyMovies {
  private final val CONTEXT_MENU_DELETE = 0

  private final val OPTIONS_MENU_SEARCH = 0
  private final val OPTIONS_MENU_CATMGR = 1
  private final val OPTIONS_MENU_PREFS = 2
  private final val OPTIONS_MENU_ABOUT = 3

  private final val ABOUT =
    "Demo application for the Manning Publications book \"Android in Practice.\"\n\nPowered by:\n http://themoviedb.org"
}
