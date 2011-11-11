package com.msi.manning.weather

import android.app.{Activity, AlertDialog}
import android.content.{DialogInterface, Intent}
import android.net.Uri
import android.os.Bundle
import android.text.{Editable, TextWatcher}
import android.util.Log
import android.view.{Menu, MenuItem, View}
import android.view.View.OnClickListener
import android.widget.{Button, EditText, Toast}

/**
 * Allow user to specify new specific location by postal code.
 * 
 * @author charliecollins
 * 
 */
class ReportSpecifyLocation extends Activity {
  import ReportSpecifyLocation._  // companion object

  private var location: EditText = _
  private var button: Button = _

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(R.layout.report_specify_location)

    location = findViewById(R.id.location).asInstanceOf[EditText]
    button = findViewById(R.id.specify_location_button).asInstanceOf[Button]

    location addTextChangedListener new TextWatcher() {
      override def onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (location.getText.toString.length > 5) {
          Toast.makeText(ReportSpecifyLocation.this, "Please enter no more than 5 digits", Toast.LENGTH_SHORT)
                        .show()
          location setText location.getText.toString.substring(0, 5)
        }
      }
      override def afterTextChanged(e: Editable) {}
      override def beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    }

    button setOnClickListener new OnClickListener() {
      override def onClick(v: View) {
        handleLoadReport()
      }
    }
  }

  override protected def onResume() {
    super.onResume()
    Log.v(Constants.LOGTAG, " " + CLASSTAG + " onResume")
  }

  override def onStart() {
    super.onStart()
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)
    menu
      .add(0, ReportSpecifyLocation.MENU_GET_REPORT, 0,
           getResources getText R.string.menu_view_report)
      .setIcon(android.R.drawable.ic_menu_more)
    menu
      .add(0, ReportSpecifyLocation.MENU_VIEW_SAVED_LOCATIONS, 1,
           getResources getText R.string.menu_goto_saved)
      .setIcon(android.R.drawable.ic_menu_myplaces)
    true
  }

  override def onMenuItemSelected(featureId: Int, item: MenuItem): Boolean =
    item.getItemId match {
      case MENU_VIEW_SAVED_LOCATIONS =>
        val intent = new Intent(ReportSpecifyLocation.this, classOf[ReportViewSavedLocations])
        startActivity(intent)
        true
      case MENU_GET_REPORT =>
        handleLoadReport()
        true
      case _ =>
        super.onMenuItemSelected(featureId, item)
    }

  private def handleLoadReport() {
    if (validate()) {
      val uri = Uri.parse("weather://com.msi.manning/loc?zip=" + location.getText.toString)
      val intent = new Intent(Intent.ACTION_VIEW, uri)
      startActivity(intent)
    }
  }

  private def validate(): Boolean = {
    var valid = true
    var validationText = new StringBuffer()
    if ((location.getText == null) || location.getText.toString.equals("")) {
      validationText append getResources.getString(R.string.message_no_location)
      valid = false
    } else if (!isNumeric(location.getText.toString) || (location.getText.toString.length != 5)) {
      validationText append getResources.getString(R.string.message_invalid_location)
      valid = false
    }
    if (!valid) {
      new AlertDialog.Builder(this)
        .setTitle(getResources.getString(R.string.alert_label))
        .setMessage(validationText.toString)
        .setPositiveButton("Continue",
           new android.content.DialogInterface.OnClickListener() {
             override def onClick(dialog: DialogInterface, arg1: Int) {
               setResult(Activity.RESULT_OK)
               finish()
             }
           }).show()
      validationText = null
    }
    valid
  }

}

object ReportSpecifyLocation {
  private final val CLASSTAG = classOf[ReportSpecifyLocation].getSimpleName
  private final val MENU_GET_REPORT = Menu.FIRST
  private final val MENU_VIEW_SAVED_LOCATIONS = Menu.FIRST + 1

  private def isNumeric(s: String): Boolean =
    try { s.toInt; true }
    catch { case _ => false }
}
