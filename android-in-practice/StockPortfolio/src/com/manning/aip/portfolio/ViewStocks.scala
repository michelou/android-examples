package com.manning.aip.portfolio

import android.app.ListActivity
import android.content.{ComponentName, Context, Intent, ServiceConnection}
import android.os.{AsyncTask, Bundle, IBinder, RemoteException}
import android.util.Log
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{BaseAdapter, Button, EditText, TextView, Toast}

import service.IStockService

/**
 * The main `Activity` of the application. This presents a simple
 * form for entering in stocks that the user wants to monitor, along with a 
 * list of the stocks that the user is monitoring. This uses the
 * [[com.manning.aip.portfolio.PortfolioManagerService]]
 * retrieve the portfolio and add stocks to it.
 * 
 * @author Michael Galpin
 *
 */
class ViewStocks extends ListActivity {
  import collection.JavaConversions._
  import ViewStocks._  // companion object

  // The list of stocks shown to the user
  private var stocks: collection.mutable.Buffer[Stock] = _
  // Service used to persist and retrieve stocks
  private var stockService: IStockService = _
  // Is the service bound currently?
  private var bound = false
  
  // Connection to the stock service, handles lifecycle events
  private val connection = new ServiceConnection {

    def onServiceConnected(className: ComponentName, service: IBinder) {
      stockService = IStockService.Stub.asInterface(service)
      Log.d(LOGGING_TAG,"Connected to service")
      try {
        stocks = asScalaBuffer(stockService.getPortfolio)
        if (stocks == null) {
          stocks = new collection.mutable.ListBuffer[Stock]
          Log.d(LOGGING_TAG, "No stocks returned from service")
        } else {
          Log.d(LOGGING_TAG, "Got "+ stocks.size +" stocks from service")
        }
        refresh()
      } catch {
        case e: RemoteException =>
        Log.e(LOGGING_TAG, "Exception retrieving portfolio from service", e)
      }
    }

    def onServiceDisconnected(className: ComponentName) {
      stockService = null
      Log.d(LOGGING_TAG,"Disconnected from service")
    }

  }

  override def onStart(){
    super.onStart()
        // create initial list
    if (!bound){
      bound = bindService(
          new Intent(ViewStocks.this, classOf[PortfolioManagerService]), 
          connection, Context.BIND_AUTO_CREATE)
      Log.d(LOGGING_TAG, "Bound to service: " + bound)
    }
    if (!bound){
      Log.e(LOGGING_TAG, "Failed to bind to service")
      throw new RuntimeException("Failed to find to service")
    }
    setListAdapter(new BaseAdapter {

      def getCount: Int =
        if (stocks == null) 0 else stocks.size

      def getItem(position: Int): AnyRef =
        if (stocks == null) null else stocks(position)

      def getItemId(position: Int): Long =
        if (stocks == null) 0L else stocks(position).getId

      def getView(position: Int, convertView0: View, parent: ViewGroup): View = {
        var convertView = convertView0
        if (convertView == null) {
          val inflater = getLayoutInflater
          convertView = inflater.inflate(R.layout.stock, parent, false)
        }
        val rowTxt = convertView.findViewById(R.id.rowTxt).asInstanceOf[TextView]
        rowTxt setText stocks(position).toString
        convertView
      }

      override def hasStableIds: Boolean = true          
    })
  }
  
  override def onPause() {
    super.onPause()
    if (bound) {
      bound = false
      unbindService(connection)
    }
  }

  override def onCreate(savedInstanceState: Bundle) {
    // Create UI elements, data loaded by `onStart`
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    // add widgets 
    val symbolIn = findViewById(R.id.inputSymbol).asInstanceOf[EditText]
    val maxIn = findViewById(R.id.inputMax).asInstanceOf[EditText]
    val minIn = findViewById(R.id.inputMin).asInstanceOf[EditText]
    val priceIn = findViewById(R.id.inputPrice).asInstanceOf[EditText]
    val quantIn = findViewById(R.id.inputQuant).asInstanceOf[EditText]

    // Add event handler to button
    val button = findViewById(R.id.btn).asInstanceOf[Button]
    button setOnClickListener new OnClickListener {
      def onClick(v: View) {
        def getDouble(v: EditText): Double = {
          val s = v.getText.toString
          try s.toDouble catch { case _ => 0D }
        }
        val symbol = symbolIn.getText.toString
        symbolIn setText ""
        val max = getDouble(maxIn)
        maxIn setText ""
        val min = getDouble(minIn)
        minIn setText ""
        val pricePaid = getDouble(priceIn)
        priceIn setText ""
        val quantity = quantIn.getText.toString.toInt
        quantIn setText ""
        val stock = new Stock(symbol, pricePaid, quantity)
        stock setMaxPrice max
        stock setMinPrice min
        // Add stock to portfolio using service in the background
        new AsyncTask[/*Stock*/AnyRef, Nothing, Stock] {
          override protected def doInBackground(newStocks: /*Stock*/AnyRef*): Stock =
            // There can be only one!
            try
              stockService addToPortfolio newStocks(0).asInstanceOf[Stock]
            catch {
              case e: RemoteException =>
                Log.e(LOGGING_TAG, "Exception adding stock " +
                  "to portfolio", e)
                null
            }
          override protected def onPostExecute(s: Stock) {
            Log.d(LOGGING_TAG, "Stock returned from service: " + s)
            if (s == null) {
              Log.w(LOGGING_TAG, "Stock returned from Service " +
                  "was null or invalid")
              Toast.makeText(ViewStocks.this, "Stock not found", 
                  Toast.LENGTH_SHORT)
            } else
              refreshStockData()
          }
        } execute stock
      }
    }
  }

  override protected def onDestroy() {
    super.onDestroy()
    // disconnect from the stock service
    unbindService(connection)
  }

  // Update stock data from the service and refresh the UI
  private def refreshStockData() {
    if (stocks != null && stocks.size > 0) {
      new AsyncTask[Nothing, Nothing, List[Stock]] {
        override protected def onPostExecute(result: List[Stock]) {
          Log.d(LOGGING_TAG, "Got new stock data from service")
          if (result != null) {
            stocks.clear()
            stocks ++= result
            refresh()
          } else
            Toast.makeText(ViewStocks.this, "Exception getting " +
                "latest stock data", Toast.LENGTH_SHORT)
        }

        override protected def doInBackground(args: Nothing*): List[Stock] =
          try
            asScalaBuffer(stockService.getPortfolio).toList
          catch {
            case e: Exception =>
              Log.e(LOGGING_TAG, "Exception getting stock data", e)
              null
          }
      }.execute()
    }
  }

  private def refresh(){
    Log.d(LOGGING_TAG, "Refreshing UI with new data")
    for (s <- stocks)
      Log.d(LOGGING_TAG, "Got stock: " + s.toString)

    val adapter = this.getListAdapter.asInstanceOf[BaseAdapter]
    adapter.notifyDataSetChanged()
  }
}

object ViewStocks {
  private final val LOGGING_TAG = "ViewStocks"
}

