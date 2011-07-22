package com.manning.aip.dealdroid

import android.app.{AlarmManager, ListActivity, PendingIntent, ProgressDialog}
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
    progressDialog setMax 2
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
    scheduleAlarmReceiver()
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

  override def onStart() {
    super.onStart()
    val forceReload = this.getIntent.getBooleanExtra(Constants.FORCE_RELOAD, false)
    this.getIntent removeExtra Constants.FORCE_RELOAD
    if (forceReload) {
      if (app.connectionPresent)
        new ParseFeedTask().execute()
      else
        showMessage(R.string.deal_list_network_unavailable)
    }
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

  // Schedule `AlarmManager` to invoke `DealAlarmReceiver` and cancel any
  // existing current `PendingIntent` we do this because we '''''also'''''
  // invoke the receiver from a `BOOT_COMPLETED` receiver so that we make
  // sure the service runs either when app is installed/started, or when
  // device boots.
  private def scheduleAlarmReceiver() {
    val alarmMgr = this.getSystemService(Context.ALARM_SERVICE).asInstanceOf[AlarmManager]
    val pendingIntent =
      PendingIntent.getBroadcast(this, 0,
                                 new Intent(this, classOf[DealAlarmReceiver]),
                                 PendingIntent.FLAG_CANCEL_CURRENT)

    // Use inexact repeating which is easier on battery (system can phase
    // events and not wake at exact times)
    alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
      Constants.ALARM_TRIGGER_AT_TIME, Constants.ALARM_INTERVAL, pendingIntent)
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
  private class ParseFeedTask extends AsyncTask[AnyRef, Int, List[Section]] {
    import collection.JavaConversions._

    override protected def onPreExecute() {
      if (progressDialog.isShowing)
        progressDialog.dismiss()
    }

    override protected def doInBackground(args: AnyRef*): List[Section] = {
      publishProgress(1)
      val sections = app.getParser.parse()
      publishProgress(2)
      sections
    }

    override protected def onProgressUpdate(progress: Int*) {
      val currentProgress = progress(0)
      if ((currentProgress == 1) && !progressDialog.isShowing) {
        progressDialog.show()
      } else if ((currentProgress == 2) && progressDialog.isShowing) {
        progressDialog.dismiss()
      }
      progressDialog setProgress currentProgress
    }

    override protected def onPostExecute(taskSectionList: List[Section]) {
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
