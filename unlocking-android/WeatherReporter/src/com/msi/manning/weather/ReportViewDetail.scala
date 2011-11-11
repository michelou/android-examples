package com.msi.manning.weather

import android.app.{Activity, ProgressDialog}
import android.content.Intent
import android.net.Uri
import android.os.{Bundle, Handler, Message}
import android.util.Log
import android.view.{Menu, MenuItem}
import android.widget.{CheckBox, CompoundButton, ImageView, TextView, Toast}
import android.widget.CompoundButton.OnCheckedChangeListener

import data.{DBHelper, WeatherForecast, WeatherRecord, YWeatherFetcher}
import data.DBHelper.Location
import service.WeatherAlertService

/**
 * Show Review detail for review item user selected, allow user to
 * enable/disable alerts, and show/react menu for other actions.
 * 
 * @author charliecollins
 * 
 */
class ReportViewDetail extends Activity {
  import ReportViewDetail._  // companion object

  private var location: TextView = _
  private var date: TextView = _
  private var condition: TextView = _
  private var forecast: TextView = _
  private var conditionImage: ImageView = _
  private var currentCheck: CheckBox = _

  private var progressDialog: ProgressDialog = _
  private var report: WeatherRecord = _
  private var reportZip: String = _
  private var deviceZip: String = _
  private var useDeviceLocation: Boolean = _

  private var savedLocation: Location = _
  private var deviceAlertEnabledLocation: Location = _

  private var dbHelper: DBHelper = _

  private val handler = new Handler() {

    override def handleMessage(msg: Message) {
      progressDialog.dismiss()
      if (report == null || (report.getCondition == null)) {
        Toast.makeText(ReportViewDetail.this, R.string.message_report_unavailable, Toast.LENGTH_SHORT).show()
      } else {
        Log.v(Constants.LOGTAG, " " + CLASSTAG + "   HANDLER report - " + report)
        location.setText(report.getCity + ", " + report.getRegion + " " + report.getCountry)
        date setText report.getDate

        val cond = new StringBuffer()
        cond append report.getCondition.display append "\n"
        cond append "Temperature: " append report.getTemp append " F "
        cond append " (wind chill " append report.getWindChill append " F)\n"
        cond append "Barometer: " append report.getPressure append " and "
        cond append report.getPressureState append "\n"
        cond append "Humidity: " append report.getHumidity
        cond append "% - Wind: " append report.getWindDirection
        cond append " " append report.getWindSpeed append "mph\n"
        cond append "Sunrise: " append report.getSunrise
        cond append " - Sunset:  " append report.getSunset
        condition setText cond.toString

        val fore = new StringBuilder()
        if (report.getForecasts != null)
          for (i <- 0 until report.getForecasts.length) {
            val fc = report getForecasts i
            fore append fc.getDay append ":\n"
            fore append fc.getCondition.display
            fore append " High:" append fc.getHigh
            fore append " F - Low:" append fc.getLow append " F"
            if (i == 0) {
              fore append "\n\n"
            }
          }
                
        forecast setText fore.toString
        val resPath = "com.msi.manning.weather:drawable/" + "cond" + report.getCondition.id
        val resId = getResources.getIdentifier(resPath, null, null)
        conditionImage setImageDrawable getResources.getDrawable(resId)
      }
    }
  }

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onCreate")

    setContentView(R.layout.report_view_detail)

    location = findViewById(R.id.view_location).asInstanceOf[TextView]
    date = findViewById(R.id.view_date).asInstanceOf[TextView]
    condition = findViewById(R.id.view_condition).asInstanceOf[TextView]
    forecast = findViewById(R.id.view_forecast).asInstanceOf[TextView]
    conditionImage = findViewById(R.id.condition_image).asInstanceOf[ImageView]
    currentCheck = findViewById(R.id.view_configure_alerts).asInstanceOf[CheckBox]

    // currentCheck listener, enable/disable alerts
    currentCheck setOnCheckedChangeListener new OnCheckedChangeListener() {
      def onCheckedChanged(button: CompoundButton, isChecked: Boolean) {
        Log.v(Constants.LOGTAG, " " + CLASSTAG +
              " onCheckedChanged - isChecked - " + isChecked)
        updateAlertStatus(isChecked)
      }
    }

    // start the service - though it may already have been started on boot
    // multiple starts don't hurt it, and if app is installed and device NOT
    // booted needs this
    startService(new Intent(this, classOf[WeatherAlertService]))
  }

  override protected def onPause() {
    super.onPause()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onPause")
    dbHelper.cleanup()
    deviceZip = WeatherAlertService.deviceLocationZIP
    if (progressDialog.isShowing) {
      progressDialog.dismiss()
    }
  }

  override def onRestart() {
    super.onRestart();
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onRestart")
  }

  override def onResume() {
    super.onResume()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onResume")
  }

  override def onStart() {
    super.onStart()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onStart")
    dbHelper = new DBHelper(this)

    // determine current location zip using convenience deviceLocationZIP member of
    // WeatherAlertService
    // (it is established there via LocationManager)
    deviceZip = WeatherAlertService.deviceLocationZIP

    val data = getIntent.getData
    // determine reportZip from Uri (or default to deviceZip)
    if ((data != null) && (data.getEncodedQuery != null)
            && (data.getEncodedQuery.length > 8)) {
      Log.v(Constants.LOGTAG, " " + CLASSTAG + " Intent data and query present, parse for zip")
      val queryString = data.getEncodedQuery
      Log.v(Constants.LOGTAG, " " + CLASSTAG + " queryString - " + queryString)
      reportZip = queryString.substring(4, 9)
      useDeviceLocation = false
    } else {
      Log.v(Constants.LOGTAG, " " + CLASSTAG + " Intent data not present, use current location")
      reportZip = deviceZip
      useDeviceLocation = true
    }

    // get saved state from db records
    savedLocation = dbHelper.get(reportZip)
    deviceAlertEnabledLocation = dbHelper.get(DBHelper.DEVICE_ALERT_ENABLED_ZIP)

    if (useDeviceLocation) {
      currentCheck setText R.string.view_checkbox_current
      currentCheck setChecked (deviceAlertEnabledLocation != null)
    } else {
      currentCheck setText R.string.view_checkbox_specific
      if (savedLocation != null) {
        currentCheck.setChecked (savedLocation.alertenabled == 1)
      }
    }
    loadReport(reportZip)
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    if (useDeviceLocation) {
      menu
        .add(0, ReportViewDetail.MENU_SPECIFY_LOCATION, 0,
             getResources getText R.string.menu_specify_location)
        .setIcon(android.R.drawable.ic_menu_edit)
    } else {
      menu
        .add(0, ReportViewDetail.MENU_VIEW_CURRENT_LOCATION, 2,
             getResources getText R.string.menu_device_location)
        .setIcon(android.R.drawable.ic_menu_mylocation)
      menu
        .add(0, ReportViewDetail.MENU_SPECIFY_LOCATION, 3,
             getResources getText R.string.menu_specify_location)
        .setIcon(android.R.drawable.ic_menu_edit)
      if (this.savedLocation != null) {
        menu
          .add(0, ReportViewDetail.MENU_REMOVE_SAVED_LOCATION, 4,
               getResources getText R.string.menu_remove_location)
          .setIcon(android.R.drawable.ic_menu_delete)
      } else {
        menu
          .add(0, ReportViewDetail.MENU_SAVE_LOCATION, 5,
               getResources getText R.string.menu_save_location)
          .setIcon(android.R.drawable.ic_menu_add)
            }
        }
    menu
      .add(0, ReportViewDetail.MENU_VIEW_SAVED_LOCATIONS, 1,
           getResources getText R.string.menu_goto_saved)
      .setIcon(android.R.drawable.ic_menu_myplaces)
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_VIEW_CURRENT_LOCATION =>
        val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + deviceZip)
        val intent = new Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
        true
      case MENU_SPECIFY_LOCATION =>
        startActivity(new Intent(ReportViewDetail.this, classOf[ReportSpecifyLocation]))
        true
      case MENU_VIEW_SAVED_LOCATIONS =>
        val intent = new Intent(ReportViewDetail.this, classOf[ReportViewSavedLocations])
        intent.putExtra("deviceZip", deviceZip)
        startActivity(intent)
        true
      case MENU_SAVE_LOCATION =>
        val loc = new Location()
        loc.alertenabled = 0
        loc.lastalert = 0
        loc.zip = reportZip;
        loc.city = report.getCity
        loc.region = report.getRegion
        dbHelper insert loc
        val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + reportZip)
        val intent = new Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
        true
      case MENU_REMOVE_SAVED_LOCATION =>
        if (this.savedLocation != null) {
          dbHelper delete reportZip
        }
        val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + reportZip)
        val intent = new Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }

  private def updateAlertStatus(isChecked: Boolean) {
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " updateAlertStatus - " + isChecked)
    // NON DEVICE
    if (!useDeviceLocation) {
      if (isChecked) {
        // no loc at all, create it as saved, and set alertenabled 1
        if (this.savedLocation == null) {
          val loc = new Location()
          loc.alertenabled = 1
          loc.lastalert = 0
          loc.zip = reportZip
          loc.city = report.getCity
          loc.region = report.getRegion
          dbHelper insert loc
          // if loc already is saved, just update alertenabled
        } else {
          savedLocation.alertenabled = 1
          dbHelper update savedLocation
        }
      } else {
        if (savedLocation != null) {
          savedLocation.alertenabled = 0
          dbHelper update savedLocation
        }
      }
    // DEVICE
    } else {
      // store whether or not user wants current device location
      // alerts in special Location with DEVICE_ALERT_ENABLED_ZIP value
      if (isChecked) {
        if (this.deviceAlertEnabledLocation == null) {
          val currentLoc = new Location()
          currentLoc.alertenabled = 1
          currentLoc.lastalert = 0
          currentLoc.zip = DBHelper.DEVICE_ALERT_ENABLED_ZIP
          dbHelper insert currentLoc
        }
      } else {
        if (deviceAlertEnabledLocation != null) {
          dbHelper delete DBHelper.DEVICE_ALERT_ENABLED_ZIP
        }
      }
    }
  }

  private def loadReport(zip: String) {
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " loadReport")
    Log.v(Constants.LOGTAG, " " + CLASSTAG + "    zip - " + zip)

    progressDialog = ProgressDialog.show(this,
      getResources.getText(R.string.view_working),
      getResources.getText(R.string.view_get_report), true, false)

    val ywh =
      try new YWeatherFetcher(zip)
      catch { case _ =>
        Toast.makeText(ReportViewDetail.this, R.string.message_no_location, Toast.LENGTH_SHORT).show()
        return
      }
    // get report in a separate thread for ProgressDialog/Handler
    // when complete send "empty" msg to handler indicating thread is done
    new Thread() {
      override def run() {
        report = ywh.getWeather
        Log.v(Constants.LOGTAG, " " + CLASSTAG + "    report - " + report)
        handler sendEmptyMessage 0
      }
    }.start()
  }
}

object ReportViewDetail {
  private final val CLASSTAG = classOf[ReportViewDetail].getSimpleName
  private final val MENU_VIEW_SAVED_LOCATIONS = Menu.FIRST
  private final val MENU_REMOVE_SAVED_LOCATION = Menu.FIRST + 1
  private final val MENU_SAVE_LOCATION = Menu.FIRST + 2
  private final val MENU_SPECIFY_LOCATION = Menu.FIRST + 3
  private final val MENU_VIEW_CURRENT_LOCATION = Menu.FIRST + 4
}
