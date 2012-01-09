package com.manning.aip.mymoviesdatabase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.{Menu, MenuItem}
import android.widget.{ImageView, TextView, Toast}

import model.{Category, Movie}

class MovieDetail extends Activity {

  private var app: MyMoviesApp = _

  private var movie: Movie = _

  private var name: TextView = _
  private var year: TextView = _
  private var image: ImageView = _
  private var tagline: TextView = _
  private var rating: TextView = _
  private var categories: TextView = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.movie_detail)

    app = getApplication.asInstanceOf[MyMoviesApp]

    name = findViewById(R.id.movie_detail_name).asInstanceOf[TextView]
    year = findViewById(R.id.movie_detail_year).asInstanceOf[TextView]
    image = findViewById(R.id.movie_detail_image).asInstanceOf[ImageView]
    tagline = findViewById(R.id.movie_detail_tagline).asInstanceOf[TextView]
    rating = findViewById(R.id.movie_detail_rating).asInstanceOf[TextView]
    categories = findViewById(R.id.movie_detail_categories).asInstanceOf[TextView]

    val intent = this.getIntent
    val movieId = intent.getLongExtra(MovieDetail.MOVIE_ID_KEY, 0)
    movie = app.getDataManager getMovie movieId
    Log.d(Constants.LOG_TAG, "MOVIE: " + movie);
    if (movie != null)
      this.populateViews()
    else
      Toast.makeText(this, "No movie found, nothing to see here", Toast.LENGTH_LONG).show()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, MovieDetail.OPTIONS_MENU_HOMEPAGE, 0, "Homepage") setIcon android.R.drawable.ic_menu_info_details
    menu.add(0, MovieDetail.OPTIONS_MENU_TRAILER, 0, "Trailer") setIcon android.R.drawable.ic_menu_view
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId match {
      case MovieDetail.OPTIONS_MENU_HOMEPAGE =>
        if ((movie.getHomepage != null) && !movie.getHomepage.equals(""))
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getHomepage)))
        else
          Toast.makeText(this, "Homepage not available", Toast.LENGTH_SHORT).show()

      case MovieDetail.OPTIONS_MENU_TRAILER =>
        if ((movie.getTrailer != null) && !movie.getTrailer.equals(""))
          startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(movie.getTrailer)))
        else
          Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show()
    }
    false
  }

  private def populateViews() {
    name setText movie.getName
    year setText movie.getYear.toString

    val imageUrl = movie.getImageUrl
    if ((imageUrl != null) && !imageUrl.equals("")) {
      if (app.getImageCache.get(imageUrl) == null)
        new DownloadTask(app.getImageCache, image) execute imageUrl
      else
        image setImageBitmap (app.getImageCache get imageUrl)
    }

    tagline setText movie.getTagline

    rating setText ("Rating: " + movie.getRating)

    categories setText this.getCategoriesString
  }

  private def getCategoriesString: String = {
    val sb = new StringBuilder()
    val cats = movie.getCategories
    var count = 0
    for (cat <- cats) {
      if (count > 0) sb append ", "
      sb append cat.getName
      count += 1
    }
    sb.toString
  }
}

object MovieDetail {
   final val MOVIE_ID_KEY = "midkey"

   private final val OPTIONS_MENU_HOMEPAGE = 0
   private final val OPTIONS_MENU_TRAILER = 1
}
