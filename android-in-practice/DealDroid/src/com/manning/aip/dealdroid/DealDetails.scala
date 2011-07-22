package com.manning.aip.dealdroid

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.{AsyncTask, Bundle}
import android.view.{Menu, MenuItem, View}
import android.widget.{ImageView, ProgressBar, TextView, Toast}

import model.Item

class DealDetails extends Activity {

  private var app: DealDroidApp = _
  private var progressBar: ProgressBar = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.dealdetails)

    app = getApplication.asInstanceOf[DealDroidApp]

    progressBar = findViewById(R.id.progress).asInstanceOf[ProgressBar]
    progressBar setIndeterminate true

    val item = app.getCurrentItem

    if (item != null) {
      val icon = findViewById(R.id.details_icon).asInstanceOf[ImageView]
      new RetrieveImageTask(icon) execute item.getPic175Url

      val title = findViewById(R.id.details_title).asInstanceOf[TextView]
      title setText item.getTitle

      val pricePrefix = getText(R.string.deal_details_price_prefix)
      val price = findViewById(R.id.details_price).asInstanceOf[TextView]
      price.setText(pricePrefix + item.getConvertedCurrentPrice)

      val msrp = findViewById(R.id.details_msrp).asInstanceOf[TextView]
      msrp setText item.getMsrp

      val quantity = findViewById(R.id.details_quantity).asInstanceOf[TextView]
      quantity setText item.getQuantity.toString

      val quantitySold = findViewById(R.id.details_quantity_sold).asInstanceOf[TextView]
      quantitySold setText item.getQuantitySold.toString

      val location = findViewById(R.id.details_location).asInstanceOf[TextView]
      location setText item.getLocation

    } else {
      showMessage("Error, no current item selected, nothing to see here")
    }
  }

  private def showMessage(text: String, longPeriod: Boolean = true) {
    val duration = if (longPeriod) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, text, duration).show()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, DealDetails.MENU_MAIL, 0, R.string.deal_details_mail_menu)
    menu.add(0, DealDetails.MENU_BROWSE, 1, R.string.deal_details_browser_menu)
    menu.add(0, DealDetails.MENU_SHARE, 2, R.string.deal_details_share_menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case DealDetails.MENU_MAIL =>
        shareDealUsingChooser("text/html")
        true
      case DealDetails.MENU_BROWSE =>
        openDealInBrowser()
        true
      case DealDetails.MENU_SHARE =>
        shareDealUsingChooser("text/*")
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }

  private def shareDealUsingChooser(typ: String) {
    val i = new Intent(Intent.ACTION_SEND)
    i setType typ
    i.putExtra(Intent.EXTRA_SUBJECT, "Subject:")
    i.putExtra(Intent.EXTRA_TEXT, createDealMessage())
    try {
      startActivity(Intent.createChooser(i, "Share deal ..."))
      //startActivity(i)  // try this to see what happens when you don't set a chooser
    } catch {
      case e: android.content.ActivityNotFoundException =>
        showMessage("There are no chooser options installed for the " + typ + " + type.",
                    false)
    }
  }

  private def openDealInBrowser() {
    val i = new Intent(Intent.ACTION_VIEW, Uri.parse(app.getCurrentItem.getDealUrl))
    startActivity(i)
  }

  // TODO not i18n'd
  private def createDealMessage(): String = {
    val item = app.getCurrentItem
    val sb = new StringBuilder
    sb append "Check out this deal:\n"
    sb.append("\nTitle:" + item.getTitle)
    sb.append("\nPrice:" + item.getConvertedCurrentPrice)
    sb.append("\nLocation:" + item.getLocation)
    sb.append("\nQuantity:" + item.getQuantity)
    sb.append("\nURL:" + item.getDealUrl)
    sb.toString
  }

  private class RetrieveImageTask(imageView: ImageView)
  extends AsyncTask[/*String*/AnyRef, /*Void*/AnyRef, Bitmap] {

    override protected def doInBackground(args: /*String*/AnyRef*): Bitmap = {
      val bitmap = DealDroidApp retrieveBitmap args(0).toString
      bitmap
    }

    override protected def onPostExecute(bitmap: Bitmap) {
      progressBar setVisibility View.GONE
      if (bitmap != null) {
        imageView setImageBitmap bitmap
        imageView setVisibility View.VISIBLE
      }
    }
  }
}

object DealDetails {
  final val MENU_MAIL = 1
  final val MENU_BROWSE = 2
  final val MENU_SHARE = 3
}
