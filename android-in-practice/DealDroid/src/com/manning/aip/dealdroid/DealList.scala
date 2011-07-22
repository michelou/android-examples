package com.manning.aip.dealdroid

import android.app.{ListActivity, ProgressDialog}
import android.content.{Context, Intent}
import android.graphics.{Bitmap, BitmapFactory}
import android.os.{AsyncTask, Bundle}
import android.view.{LayoutInflater, Menu, MenuItem, View, ViewGroup}
import android.widget.{AdapterView, ArrayAdapter, ImageView, ListView, Spinner, TextView, Toast}
import android.widget.AdapterView.OnItemSelectedListener

import model.{Item, Section}

import java.util.{ArrayList => JArrayList, List => JList}

class DealList extends ListActivity {
  import scala.collection.JavaConversions._

  private var app: DealDroidApp = _
  private var sections: JList[Section] = _
  private var items: JList[Item] = _
  private var dealsAdapter: DealsAdapter = _
  private var spinnerAdapter: ArrayAdapter[Section] = _
  private var currentSelectedSection: Int = _
  private var progressDialog: ProgressDialog = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.deallist)

    progressDialog = new ProgressDialog(this)
    progressDialog setCancelable false
    progressDialog setMessage getString(R.string.deal_list_retrieving_data)

    // Use Application object for app wide state
    app = getApplication.asInstanceOf[DealDroidApp]
    sections = app.getSectionList

    // construct Adapter with empty items collection to start
    items = new JArrayList[Item]
    dealsAdapter = new DealsAdapter(items)

    // ListView adapter (this class extends ListActivity)
    setListAdapter(dealsAdapter)

    // get Sections list from application (parsing feed if necessary)
    if (sections.isEmpty)
      if (app.connectionPresent)
        new ParseFeedTask().execute()
      else
        showMessage(R.string.deal_list_network_unavailable)
    else
      resetListItems(sections.get(0).items)

    // Spinner for choosing a Section
    val sectionSpinner = findViewById(R.id.section_spinner).asInstanceOf[Spinner]
    spinnerAdapter =
      new ArrayAdapter[Section](DealList.this,
                                android.R.layout.simple_spinner_item,
                                sections)
    spinnerAdapter setDropDownViewResource android.R.layout.simple_spinner_dropdown_item
    sectionSpinner setAdapter spinnerAdapter
    sectionSpinner setOnItemSelectedListener new OnItemSelectedListener() {
      override def onItemSelected(parentView: AdapterView[_], selectedItemView: View, position: Int, id: Long) {
        if (currentSelectedSection != position) {
          currentSelectedSection = position
          resetListItems(sections.get(position).items)
        }
      }

      override def onNothingSelected(parentView: AdapterView[_]) {
        // do nothing
      }
    }
  }

  private def showMessage(resId: Int, longPeriod: Boolean = true) {
    val duration = if (longPeriod) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, getString(resId), duration).show()
  }

  private def resetListItems(newItems: List[Item]) {
    items.clear()
    items addAll newItems
    dealsAdapter.notifyDataSetChanged()
  }

  override protected def onListItemClick(listView: ListView, view: View, position: Int, id: Long) {
    view setBackgroundColor android.R.color.background_light
    app setCurrentItem sections.get(currentSelectedSection).items(position)
    val dealDetails = new Intent(DealList.this, classOf[DealDetails])
    startActivity(dealDetails)
  }

  override def onPause() {
    if (progressDialog.isShowing)
      progressDialog.dismiss()

    super.onPause()
  }
   
  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    menu.add(0, DealList.MENU_REPARSE, 0, R.string.deal_list_reparse_menu)
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean =
    item.getItemId match {
      case DealList.MENU_REPARSE =>
        if (app.connectionPresent)
          new ParseFeedTask().execute()
        else
          showMessage(R.string.deal_list_network_unavailable)
        true
      case _ =>
        super.onOptionsItemSelected(item)
    }  

  // Use a custom Adapter to control the layout and views
  private class DealsAdapter(items: JList[Item])
  extends ArrayAdapter[Item](DealList.this, R.layout.list_item, items) {
 
    override def getView(position: Int, view: View, parent: ViewGroup): View = {
      var convertView = view // Scala params are read-only
      if (convertView == null) {
        val inflater =
          getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
        convertView = inflater.inflate(R.layout.list_item, parent, false)
      }

      // use ViewHolder here to prevent multiple calls to findViewById (if you have a large collection)
      val text = convertView.findViewById(R.id.deal_title).asInstanceOf[TextView]
      val image = convertView.findViewById(R.id.deal_img).asInstanceOf[ImageView]
      image setImageBitmap BitmapFactory.decodeResource(getResources, R.drawable.ddicon)

      val item = getItem(position)

      if (item != null) {
        text setText item.getTitle
        app getImage item.getItemId match {
          case Some(bitmap) =>
            image setImageBitmap bitmap
          case _ =>
            // put item ID on image as TAG for use in task
            image setTag item.getItemId
            // separate thread/via task, for retrieving each image
            // (note that this is brittle as is, should stop all threads in onPause)
            new RetrieveImageTask(image) execute item.getSmallPicUrl
        }
      }

      convertView
    }
  }
   
  // Use an AsyncTask<Params, Progress, Result> to easily perform tasks off of the UI Thread
  private class ParseFeedTask extends AsyncTask[AnyRef, AnyRef, List[Section]] {
    import collection.JavaConversions._

    override protected def onPreExecute() {
      progressDialog.show()
    }

    override protected def doInBackground(args: AnyRef*): List[Section] = {
      app.getParser.parse()
    }

    override protected def onPostExecute(taskSectionList: List[Section]) {
      if (progressDialog.isShowing)
        progressDialog.dismiss()

      if (!taskSectionList.isEmpty) {
        sections.clear()
        sections addAll taskSectionList
        spinnerAdapter.notifyDataSetChanged()

        resetListItems(sections.get(0).items)
      } else
        showMessage(R.string.deal_list_missing_data)
    }
  }

  private class RetrieveImageTask(imageView: ImageView)
  extends AsyncTask[/*String*/AnyRef, AnyRef, Bitmap] {

    override protected def doInBackground(args: /*String*/AnyRef*): Bitmap = {
      DealDroidApp retrieveBitmap args(0).toString
    }

    override protected def onPostExecute(bitmap: Bitmap) {
      if (bitmap != null) {
        imageView setImageBitmap bitmap
        app addImage(imageView.getTag.asInstanceOf[Long], bitmap)
        imageView setTag null
      }
    }
  }

}

object DealList {
  private final val MENU_REPARSE = 0
}
