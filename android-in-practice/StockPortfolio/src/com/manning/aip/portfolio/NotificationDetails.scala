package com.manning.aip.portfolio

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

/**
 * An Activity that is started when a user opens a Notification.
 * 
 * @author Michael Galpin
 *
 */
class NotificationDetails extends Activity {

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    this.setContentView(R.layout.details)

    val stock = this.getIntent.getParcelableExtra("stock").asInstanceOf[Stock]
    val nameLabel = findViewById(R.id.name).asInstanceOf[TextView]
    if (stock == null) {
      nameLabel setText "No stock passed in to display"
      return
    }
    
    nameLabel.setText(stock.getName + "(" + stock.getSymbol + ")")
    
    val current = stock.getCurrentPrice
    val currentLabel = findViewById(R.id.current).asInstanceOf[TextView]
    currentLabel.setText("Current Price: $" + current)

    val minLabel = findViewById(R.id.min).asInstanceOf[TextView]
    if (current < stock.getMinPrice) {
      minLabel.setText("Current price is less than minimum price $" + 
          stock.getMinPrice)
      minLabel setTextColor 0xFFFF0000
    } else {
      minLabel.setText("Minimum price: $" + stock.getMinPrice)
    }
    
    val maxLabel = findViewById(R.id.max).asInstanceOf[TextView]
    if (current > stock.getMaxPrice) {
      maxLabel.setText("Current price is more than maximum price $" + 
          stock.getMaxPrice)
      maxLabel setTextColor 0xFF00FF00
    } else {
      maxLabel.setText("Maximum price: $" + stock.getMaxPrice)
    }
  }

}
