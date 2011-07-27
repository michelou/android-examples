package com.manning.aip.portfolio

import android.app.{Activity, IntentService}
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button

import AddStockService._

class AddStockService extends IntentService(NAME) {
	
  override protected def onHandleIntent(request: Intent) {
    val stock = request getParcelableExtra EXTRA_STOCK
    val db = new StocksDb(this)
    db addStock stock
  }

}

object AddStockService {
  private final val NAME = "AddStockService"

  final val EXTRA_STOCK = "Stock"

  class StockActivity extends Activity {
    private var stock: Stock = _
    override def onCreate(savedInstance: Bundle) {
      val button = findViewById(R.id.btn).asInstanceOf[Button]
      button setOnClickListener new OnClickListener {
        override def onClick(v: View) {
          val request = new Intent(StockActivity.this, classOf[AddStockService])
          request.putExtra(EXTRA_STOCK, stock)
          startService(request)
        }		
      }
    }
  }
}

