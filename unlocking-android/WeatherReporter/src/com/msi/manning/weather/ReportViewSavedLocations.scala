package com.msi.manning.weather

import android.app.{ListActivity, ProgressDialog}
import android.content.Intent
import android.net.Uri
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.widget.{ArrayAdapter, ListAdapter, ListView, TextView}

import data.DBHelper
import data.DBHelper.Location

import scala.collection.JavaConversions._

/**
 * Allow user to choose previously saved locations.
 * 
 * @author charliecollins
 * 
 */
class ReportViewSavedLocations extends ListActivity {
  import ReportViewSavedLocations._  // companion object

  private var dbHelper: DBHelper = _
  private var progressDialog: ProgressDialog = _
  private var empty: TextView = _
  private var locations: List[Location] = _
  private var adapter: ListAdapter = _

  private val handler = new Handler() {
    override def handleMessage(msg: Message) {
      Log.v(Constants.LOGTAG, " " + CLASSTAG + " worker thread done, setup list")
      progressDialog.dismiss()
      if (locations == null || locations.size == 0) {
        empty setText "No Data"
      } else {
        adapter = new ArrayAdapter[Location](ReportViewSavedLocations.this, R.layout.list_item_1, locations)
        setListAdapter(adapter)
      }
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.report_view_saved_locations)
    getListView setEmptyView findViewById(R.id.view_saved_locations_empty)
    empty = findViewById(R.id.view_saved_locations_empty).asInstanceOf[TextView]
    dbHelper = new DBHelper(this)
    loadLocations()
  }

  override protected def onPause() {
    super.onResume()
    dbHelper.cleanup()
  }

  override protected def onResume() {
    super.onResume()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu
      .add(0, ReportViewSavedLocations.MENU_SPECIFY_LOCATION, 0,
           getResources getText R.string.menu_specify_location)
      .setIcon(android.R.drawable.ic_menu_edit);
    menu
      .add(0, ReportViewSavedLocations.MENU_VIEW_CURRENT_LOCATION, 1,
           getResources getText R.string.menu_device_location)
      .setIcon(android.R.drawable.ic_menu_mylocation)

    true
  }

  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " selected list item")
    val loc = locations(position)
    val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + loc.zip)
    val intent = new Intent(Intent.ACTION_VIEW, uri)
    startActivity(intent)
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_VIEW_CURRENT_LOCATION =>
        val deviceZip = getIntent getStringExtra "deviceZip"
        val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + deviceZip)
        val intent = new Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
        true
      case MENU_SPECIFY_LOCATION =>
        startActivity(new Intent(ReportViewSavedLocations.this, classOf[ReportSpecifyLocation]))
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }

  private def loadLocations() {
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " loadLocations")
    progressDialog = ProgressDialog.show(this, " Working...", " Retrieving saved locations", true, false)
    new Thread() {
      override def run() {
        locations = dbHelper.getAll
        handler sendEmptyMessage 0
      }
    }.start()
  }
}

object ReportViewSavedLocations {
  private final val CLASSTAG = classOf[ReportViewSavedLocations].getSimpleName
  private final val MENU_SPECIFY_LOCATION = Menu.FIRST
  private final val MENU_VIEW_CURRENT_LOCATION = Menu.FIRST + 1
}
