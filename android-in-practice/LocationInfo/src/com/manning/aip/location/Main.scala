package com.manning.aip.location

import android.app.Activity
import android.content.{Context, Intent}
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.{ArrayAdapter, Button, ListView, TextView}

class Main extends Activity with OnItemClickListener {
  import Main._  // companion object

  private var locationMgr: LocationManager = _
  private var providersList: ListView = _

  private var getLoc: Button = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    locationMgr = getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    val adapter = new ArrayAdapter[String](this, android.R.layout.simple_list_item_1, locationMgr.getAllProviders)
    providersList = findViewById(R.id.location_providers).asInstanceOf[ListView]
    providersList setAdapter adapter

    providersList setOnItemClickListener this

    getLoc = findViewById(R.id.getloc_button).asInstanceOf[Button]
    getLoc setOnClickListener new OnClickListener() {
      def onClick(v: View) {
        startActivity(new Intent(Main.this, classOf[GetLocationWithGPS]))
      }
    }
  }

  override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long) {
    val textView = view.asInstanceOf[TextView]
    val providerName = textView.getText.toString
    val intent = new Intent(Main.this, classOf[ProviderDetail])
    intent.putExtra(PROVIDER_NAME, providerName)
    startActivity(intent)
  }
}

object Main {
  final val LOG_TAG = "LocationInfo"
  final val PROVIDER_NAME = "PROVIDER_NAME"
}
