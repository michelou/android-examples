package com.manning.aip.portfolio

import android.content.Context
import android.database.Cursor
import android.database.sqlite.{SQLiteDatabase, SQLiteOpenHelper, SQLiteStatement}
import android.util.Log

/**
 * A data access object for persisting and retrieving stock data. This uses
 * a SQLite database for persistence and retrieval.
 * 
 * The constructor takes a `Context` object, usually the `Service` or
 * `Activity` that created this instance. This will initialize the
 * SQLiteOpenHelper used for the database, and pre-compile the insert
 * and update SQL statements.
 * 
 * @param   ctx The `Context` that created this instance
 *
 * @author Michael Galpin
 *
 */
class StocksDb(private val context: Context) {
  import StocksDb._  // companion object

  // initialize the database helper
  private val helper = new SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION){
    override def onCreate(db: SQLiteDatabase) {
      db execSQL CREATE_TABLE
      Log.d(TAG, "Created table: \n" + CREATE_TABLE)
    }
    override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
      throw new UnsupportedOperationException()
    }
  }

  // open the database
  private val db = helper.getWritableDatabase

  // pre-compile statements
  private var stmt = db compileStatement INSERT_SQL
  private var updateStmt = db compileStatement UPDATE_SQL

  /**
   * Saves a `Stock` to the database.
   * 
   * @param   stock A `Stock` instance that will be added to the database.
   * @return  A `Stock`instance with its data refreshed from the
   *          database, including its database-assigned ID.
   */
  def addStock(stock: Stock): Stock = {
    Log.d(TAG, "Adding stock to db, stock="+stock)
    stmt.bindString(1, stock.getSymbol)
    stmt.bindDouble(2, stock.getMaxPrice)
    stmt.bindDouble(3, stock.getMinPrice)
    stmt.bindDouble(4, stock.getPricePaid)
    stmt.bindLong(5, stock.getQuantity)
    stmt.bindDouble(6, stock.getCurrentPrice)
    stmt.bindString(7, stock.getName)
    val id = stmt.executeInsert().toInt
    new Stock(stock, id)
  }

  /**
   * Updates the current price of a `Stock` stored in the database.
   * 
   * @param   stock The `Stock` being updated.
   */
  def updateStockPrice(stock: Stock) {
    Log.d(TAG, "Updating stock price in DB stock="+stock.toString)
    updateStmt.bindDouble(1, stock.getCurrentPrice)
    updateStmt.bindLong(2, stock.getId)
    updateStmt.execute()
  }

  /**
   * Retrieve all of the`Stock`s stored in the database.
   * 
   * @return  List of all of the Stocks stored in the database.
   */
  def getStocks: List[Stock] = {
    Log.d(TAG, "Getting stocks from DB")
    val results = db.rawQuery(READ_SQL, null)
    val stocks = new collection.mutable.ListBuffer[Stock]
    if (results.moveToFirst()){
      val idCol = results getColumnIndex ID
      val symbolCol = results getColumnIndex SYMBOL
      val maxCol = results getColumnIndex MAX_PRICE
      val minCol = results getColumnIndex MIN_PRICE
      val priceCol = results getColumnIndex PRICE_PAID
      val quanitytCol = results getColumnIndex QUANTITY
      val currentPriceCol = results getColumnIndex CURRENT_PRICE
      val nameCol = results getColumnIndex NAME
      do {
        val stock =
          new Stock(results getString symbolCol, results getDouble priceCol, 
                    results getInt quanitytCol, results getInt idCol)
        stock setMaxPrice results.getDouble(maxCol)
        stock setMinPrice results.getDouble(minCol)
        stock setCurrentPrice results.getDouble(currentPriceCol)
        stock setName results.getString(nameCol)
        Log.d(TAG, "Stock from db = " + stock.toString)
        stocks += stock
      } while (results.moveToNext())
    }
    if (!results.isClosed){
      results.close()
    }
    stocks.toList
  }

  /**
   * Method to close the underlying database connection.
   */
  def close() {
    helper.close()
  }
}

object StocksDb {
  private final val TAG = "StocksDb"

  // database metadata
  private final val DB_NAME = "stocks.db"
  private final val DB_VERSION = 1

  private final val TABLE_NAME = "stock"

  // column names
  private final val ID = "id"
  private final val SYMBOL = "symbol"
  private final val MAX_PRICE = "max_price"
  private final val MIN_PRICE = "min_price"
  private final val PRICE_PAID = "price_paid"
  private final val QUANTITY = "quantity"
  private final val CURRENT_PRICE = "current_price"
  private final val NAME = "name"

  // SQL statements
  private final val CREATE_TABLE = "CREATE TABLE " + TABLE_NAME +
      " ("+ID+" INTEGER PRIMARY KEY, "+SYMBOL+" TEXT, "+
      MAX_PRICE+" DECIMAL(8,2), " + MIN_PRICE+" DECIMAL(8,2), " +
      PRICE_PAID+ " DECIMAL(8,2), " + QUANTITY + " INTEGER, " +
      CURRENT_PRICE + " DECIMAL(8,2), "+NAME+" TEXT)"
  private final val INSERT_SQL = "INSERT INTO " + TABLE_NAME +
      " ("+SYMBOL+", "+MAX_PRICE+", "+MIN_PRICE+", "+PRICE_PAID+
      ", "+QUANTITY+", " + CURRENT_PRICE+", "+NAME+") " +
      "VALUES (?,?,?,?,?,?,?)"
  private final val READ_SQL = "SELECT "+ID+", "+SYMBOL+", " +
      MAX_PRICE+", " + MIN_PRICE +", "+PRICE_PAID+", "+ 
      QUANTITY+", " +CURRENT_PRICE+ ", "+NAME+" FROM " + 
      TABLE_NAME
  private final val UPDATE_SQL = "UPDATE " + TABLE_NAME + 
    " SET "+CURRENT_PRICE+"=? WHERE "+ID+"=?"
}

