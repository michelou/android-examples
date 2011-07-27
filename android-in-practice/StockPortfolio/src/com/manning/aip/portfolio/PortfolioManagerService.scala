package com.manning.aip.portfolio

import java.io.{BufferedReader, IOException, InputStreamReader}

import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient

import android.app.{Notification, NotificationManager, PendingIntent, Service}
import android.content.{Context, Intent}
import android.os.{IBinder, RemoteException}
import android.util.Log
import android.widget.RemoteViews

import service.IStockService

/**
 * Background `Service` used for managing the list of stocks in a
 * user's portfolio, periodically updating stock price information on those
 * stocks, and publishing the user if stock prices go too high or too low.
 * 
 * @author Michael Galpin
 *
 */
class PortfolioManagerService extends Service {
  import PortfolioManagerService._  // companion object

  // This is a data access object used for persisting stock information.
  private var db: StocksDb = _

  // Timestamp of last time stock data was downloaded from the Itnernet
  private var timestamp = 0L

  override def onCreate() {
    super.onCreate();
    db = new StocksDb(this)
  }

  override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
    if (db == null)
      db = new StocksDb(this)

    try
      updateStockData()
    catch {
      case e: IOException =>
        Log.e(TAG, "Exception updating stock data", e)
    }
    Service.START_STICKY
  }

  override def onDestroy() {
    super.onDestroy()
    db.close()
  }

  override def onBind(intent: Intent): IBinder = {
    if (db == null)
      db = new StocksDb(this)

    // implement the IStockService interface defined in AIDL 
    new IStockService.Stub() {
      @throws(classOf[RemoteException])
      def addToPortfolio(stock: Stock): Stock = {
        Log.d(TAG, "Adding stock="+stock)
        var s = db addStock stock
        Log.d(TAG, "Stock added to db")
        try {
          updateStockData()
          for (x <- db.getStocks) {
            if (x.getSymbol equalsIgnoreCase stock.getSymbol) {
              s = x
            }
          }
          Log.d(TAG, "Stock data updated")
        } catch {
          case e: IOException =>
            Log.e(TAG, "Exception updating stock data", e)
            throw new RemoteException
        }
        s
      }
      @throws(classOf[RemoteException])
      def getPortfolio: java.util.List[Stock] = {
        import collection.JavaConversions._
        Log.d(TAG, "Getting portfolio")
        val stocks = db.getStocks
        val currTime = System.currentTimeMillis
        if (currTime - timestamp <= MAX_CACHE_AGE){
          Log.d(TAG, "Fresh cache, returning it")
          stocks
        }
        // else cache is stale, refresh it
        else try {
          Log.d(TAG, "Stale cache, refreshing it")
          val newStocks = fetchStockData(stocks)
          Log.d(TAG, "Got new stock data, updating cache")
          updateCachedStocks(newStocks)
          newStocks
        } catch {
          case e: Exception =>
            Log.e(TAG, "Exception getting stock data", e)
            throw new RemoteException()
        }
      }
    }
  }

  @throws(classOf[IOException])
  private def updateStockData() {
    val stocks = fetchStockData(db.getStocks)
    updateCachedStocks(stocks)
  }

  private def updateCachedStocks(stocks: List[Stock]) {
    Log.d(TAG, "Got new stock data to update cache with")
    timestamp = System.currentTimeMillis
    for (stock <- stocks) {
      Log.d(TAG, "Updating cache with stock=" + stock.toString)
      db updateStockPrice stock
    }
    Log.d(TAG, "Cache updated, checking for alerts")
    checkForAlerts(stocks)
  }

  @throws(classOf[IOException])
  private def fetchStockData(stocks: List[Stock]): List[Stock] = {
    Log.d(TAG, "Fetching stock data from Yahoo")
    val newStocks = new collection.mutable.ListBuffer[Stock]
    if (stocks.length > 0) {
      val sb = new StringBuilder
      for (stock <- stocks)
        sb append stock.getSymbol append '+'
      sb.deleteCharAt(sb.length - 1)
      val urlStr = 
        "http://finance.yahoo.com/d/quotes.csv?f=sb2n&s=" + sb.toString
      val client = new DefaultHttpClient
      val request = new HttpGet(urlStr.toString)
      val response = client execute request
      val reader = new BufferedReader(
          new InputStreamReader(response.getEntity.getContent))
      var line = reader.readLine()
      var i = 0
      Log.d(TAG, "Parsing stock data from Yahoo")
      while (line != null) {
        Log.d(TAG, "Parsing: " + line)
        val values = line split ","
        val stock = new Stock(stocks(i), stocks(i).getId)
        stock setCurrentPrice values(1).toDouble
        stock setName values(2)
        Log.d(TAG, "Parsed Stock: " + stock)
        newStocks += stock
        line = reader.readLine()
        i += 1
      }
    }
    newStocks.toList
  }

  private def checkForAlerts(stocks: Iterable[Stock]) {
    try {
      for (stock <- stocks) {
        val current = stock.getCurrentPrice
        if (current > stock.getMaxPrice)
          createHighPriceNotification(stock)
        else if (current < stock.getMinPrice)
          createLowPriceNotification(stock)
      }
    } finally {
      AlarmReceiver.releaseLock()
      stopSelf()
    }
  }

  private def createHighPriceNotification(stock: Stock) {
    val mgr =
      getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]
    val dollarBill = R.drawable.dollar_icon
    val shortMsg = "High price alert: " + stock.getSymbol
    val time = System.currentTimeMillis
    val n = new Notification(dollarBill, shortMsg, time)
    
    val title = stock.getName
    val msg = "Current price $" + stock.getCurrentPrice + " is high"
    val i = new Intent(this, classOf[NotificationDetails])
    i.putExtra("stock", stock)
    val pi = PendingIntent.getActivity(this, 0, i, 0)
  
    n.setLatestEventInfo(this, title, msg, pi)
    n.defaults |= Notification.DEFAULT_SOUND
    val steps = Array[Long](0, 500, 100, 200, 100, 200)
    n.vibrate = steps
    n.ledARGB = 0x80009500
    n.ledOnMS = 250
    n.ledOffMS = 500
    n.flags |= Notification.FLAG_SHOW_LIGHTS
    mgr.notify(HIGH_PRICE_NOTIFICATION, n)
  }

  private def createLowPriceNotification(stock: Stock) {
    val mgr = 
      getSystemService(Context.NOTIFICATION_SERVICE).asInstanceOf[NotificationManager]    
    val dollarBill = R.drawable.dollar_icon
    val shortMsg = "Low price alert: " + stock.getSymbol
    val time = System.currentTimeMillis
    val n = new Notification(dollarBill, shortMsg, time)

    val pkg = getPackageName
    val view = new RemoteViews(pkg, R.layout.notification_layout)
    val msg = "Current price $" + stock.getCurrentPrice + " is low"
    view.setTextViewText(R.id.notification_message, msg)
    n.contentView = view
    val i = new Intent(this, classOf[NotificationDetails])
    i.putExtra("stock", stock)
    val pi = PendingIntent.getActivity(this, 0, i, 0)
    n.contentIntent = pi

    n.defaults |= Notification.DEFAULT_SOUND
    val steps = Array[Long](0, 500, 100, 500, 100, 500, 100, 500)
    n.vibrate = steps
    n.ledARGB = 0x80A80000
    n.ledOnMS = 1
    n.ledOffMS = 0
    n.flags |= Notification.FLAG_SHOW_LIGHTS
    mgr.notify(LOW_PRICE_NOTIFICATION, n)
  }
}

object PortfolioManagerService {
  private final val TAG = "PortfolioManagerService"

  // How old downloaded stock data can be and still be used
  private final val MAX_CACHE_AGE = 15*60*1000 // 15 minutes

  // Types of Notifications
  private final val HIGH_PRICE_NOTIFICATION = 1
  private final val LOW_PRICE_NOTIFICATION = 0
}

