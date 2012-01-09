package com.manning.aip.mymoviesdatabase

import android.app.{Activity, AlertDialog, ProgressDialog}
import android.content.{DialogInterface, Intent}
import android.os.{AsyncTask, Bundle}
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.{AdapterView, ArrayAdapter, Button, EditText}
import android.widget.{ListView, TextView, Toast}
import android.widget.AdapterView.OnItemClickListener

import model.{Movie, MovieSearchResult}
import xml.{MovieFeed, TheMovieDBXmlPullFeedParser}

import java.util.{ArrayList => JArrayList, List => JList}

import scala.collection.JavaConversions._

class MovieSearch extends Activity {

  private var app: MyMoviesApp = _

  private var parser: MovieFeed = _
  private var movies: JList[MovieSearchResult] = _
  private var adapter: ArrayAdapter[MovieSearchResult] = _

  private var input: EditText = _
  private var search: Button = _
  private var listView: ListView = _

  private var progressDialog: ProgressDialog = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.movie_search)

    app = getApplication.asInstanceOf[MyMoviesApp]

    parser = new TheMovieDBXmlPullFeedParser()

    progressDialog = new ProgressDialog(this)
    progressDialog setIndeterminate true
    progressDialog setCancelable false
    progressDialog setMessage "Retrieving data..."

    input = findViewById(R.id.search_input).asInstanceOf[EditText]
    search = findViewById(R.id.search_submit).asInstanceOf[Button]
    search setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        if (!isTextViewEmpty(input))
          // do network trip/parsing in separate thread
          new ParseMovieSearchTask() execute input.getText.toString
        else
          Toast.makeText(MovieSearch.this, "Search term required", Toast.LENGTH_SHORT).show()
      }
    }

    movies = new JArrayList[MovieSearchResult]()
    listView = findViewById(R.id.search_results_list).asInstanceOf[ListView]
    listView setEmptyView findViewById(R.id.search_results_list_empty)
    adapter = new ArrayAdapter[MovieSearchResult](this, android.R.layout.simple_list_item_1, movies)
    listView setAdapter adapter
    listView setOnItemClickListener new OnItemClickListener() {
      def onItemClick(parent: AdapterView[_], v: View, index: Int, id: Long) {
        val movieSearchResult: MovieSearchResult = movies get index
        // do network trip/parsing in separate thread
        new ParseMovieTask() execute movieSearchResult.getProviderId
      }
    }
  }

  override protected def onPause() {
    if (progressDialog.isShowing) progressDialog.dismiss()
    super.onPause()
  }

  private def isTextViewEmpty(textView: TextView): Boolean =
    !((textView != null) && (textView.getText != null) &&
      (textView.getText.toString != null) && !textView.getText.toString.equals(""))

  private class ParseMovieSearchTask extends AsyncTask[/*String*/AnyRef, Int, List[MovieSearchResult]] {

    override protected def onPreExecute() {
      super.onPreExecute();
      if (!progressDialog.isShowing) progressDialog.show()
    }

    override protected def doInBackground(args: AnyRef*): List[MovieSearchResult] = {
      val moviesFromTask = parser search args(0).toString
      moviesFromTask
    }

    override protected def onPostExecute(moviesFromTask: List[MovieSearchResult]) {
      super.onPostExecute(moviesFromTask)
      if (progressDialog.isShowing) progressDialog.hide()
         
      movies.clear()
      movies addAll moviesFromTask
      Log.d(Constants.LOG_TAG, " movies size after parse: " + movies.size())
      adapter.notifyDataSetChanged()
    }
  }

  private class ParseMovieTask extends AsyncTask[/*String*/AnyRef, Int, Movie] {

    override protected def onPreExecute() {
      super.onPreExecute()
      if (!progressDialog.isShowing) progressDialog.show()
    }

    override protected def doInBackground(args: AnyRef*): Movie = {         
      val movie = parser get args(0).toString
      movie
    }

    override protected def onPostExecute(movie: Movie) {
      super.onPostExecute(movie)
      if (progressDialog.isShowing) progressDialog.hide()

      if (movie != null) {
        new AlertDialog.Builder(MovieSearch.this)
          .setTitle("Add Movie?")
          .setMessage(movie.getName)
          .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            def onClick(d: DialogInterface, i: Int) {
              // let the activity check if movie exists, not manager
              // (activity has contextual info to know the check is necessary)
              val exists = app.getDataManager findMovie movie.getName
              if (exists == null) {
                app.getDataManager saveMovie movie
                startActivity(new Intent(MovieSearch.this, classOf[MyMovies]))
              } else {
                Toast.makeText(MovieSearch.this, "Movie already exists", Toast.LENGTH_SHORT).show()
              }
            }
          })
          .setNegativeButton("No", new DialogInterface.OnClickListener() {
            def onClick(d: DialogInterface, i: Int) {}
          }).show()
      } else {
        Toast.makeText(MovieSearch.this, "Problem parsing movie, no result, please try again later",
                     Toast.LENGTH_SHORT).show()
      }
    }
  }
}
