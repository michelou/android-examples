package com.manning.aip

import java.io.IOException

import org.apache.http.client.{HttpClient, HttpRequestRetryHandler}
import org.apache.http.conn.params.{ConnManagerParams, ConnPerRouteBean}
import org.apache.http.conn.scheme.{PlainSocketFactory, Scheme, SchemeRegistry}
import org.apache.http.impl.client.{AbstractHttpClient, DefaultHttpClient}
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager
import org.apache.http.params.{BasicHttpParams, HttpConnectionParams}
import org.apache.http.params.HttpParams
import org.apache.http.params.HttpProtocolParams
import org.apache.http.protocol.HttpContext

import android.app.{AlertDialog, ListActivity}
import android.content.{DialogInterface, IntentFilter}
import android.content.DialogInterface.OnClickListener
import android.net.ConnectivityManager
import android.os.{Bundle, Handler, Message}
import android.os.Handler.Callback
import android.util.Log
import android.view.View
import android.widget.{AdapterView, Button, ListView, Toast}
import android.widget.AdapterView.OnItemLongClickListener

class MyMovies extends ListActivity with Callback with OnItemLongClickListener {
  import MyMovies._  // companion object

  private var adapter: MovieAdapter = _

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    setContentView(R.layout.main)

    val listView = getListView
    listView setOnItemLongClickListener this

    val backToTop = getLayoutInflater.inflate(R.layout.list_footer, null).asInstanceOf[Button]
    backToTop.setCompoundDrawablesWithIntrinsicBounds(getResources
               .getDrawable(android.R.drawable.ic_menu_upload), null, null,
               null);
    listView.addFooterView(backToTop, null, true)

    this.adapter = new MovieAdapter(this)
    listView setAdapter this.adapter
    listView setItemsCanFocus false

    registerReceiver(new ConnectionChangedBroadcastReceiver(),
               new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

    new UpdateNoticeTask(new Handler(this)).execute()
  }

  def backToTop(view: View) {
    getListView setSelection 0
  }

  override protected def onListItemClick(l: ListView, v: View, position: Int, id: Long) {
    this.adapter.toggleMovie(position)
    this.adapter.notifyDataSetInvalidated()
  }

  def onItemLongClick(arg0: AdapterView[_], arg1: View,
                      position: Int, arg3: Long): Boolean = {
    Toast.makeText(this, "Getting details...", Toast.LENGTH_LONG).show()
    val movie = adapter getItem position
    new GetMovieRatingTask(this).execute(movie.getId)
    false
  }

  def handleMessage(msg: Message): Boolean = {
    val updateNotice = msg.getData getString "text"
    val dialog = new AlertDialog.Builder(this)
    dialog setTitle "What's new"
    dialog setMessage updateNotice
    dialog setIcon android.R.drawable.ic_dialog_info
    dialog.setPositiveButton(getString(android.R.string.ok),
      new OnClickListener() {
        def onClick(dialog: DialogInterface, which: Int) {
          dialog.dismiss();
        }
      })
    dialog.show()
    false
  }
}

object MyMovies {
  private final val httpClient: AbstractHttpClient = {
    val schemeRegistry = new SchemeRegistry()
    schemeRegistry.register(
      new Scheme("http", PlainSocketFactory.getSocketFactory, 80))

    val connManagerParams = new BasicHttpParams()
    ConnManagerParams.setMaxTotalConnections(connManagerParams, 5)
    ConnManagerParams.setMaxConnectionsPerRoute(connManagerParams,
               new ConnPerRouteBean(5))
    ConnManagerParams.setTimeout(connManagerParams, 15 * 1000);

    val cm = new ThreadSafeClientConnManager(connManagerParams, schemeRegistry)

    val clientParams = new BasicHttpParams()
    HttpProtocolParams.setUserAgent(clientParams, "MyMovies/1.0")
    HttpConnectionParams.setConnectionTimeout(clientParams, 15 * 1000)
    HttpConnectionParams.setSoTimeout(clientParams, 15 * 1000)
    new DefaultHttpClient(cm, clientParams)
  }

  private final val retryHandler = new DefaultHttpRequestRetryHandler(5, false) {
    override def retryRequest(exception: IOException, executionCount: Int,
                              context: HttpContext): Boolean = {
      if (!super.retryRequest(exception, executionCount, context)) {
        Log.d("HTTP retry-handler", "Won't retry")
        return false
      }
      try Thread.sleep(2000)
      catch { case e: InterruptedException => }
      Log.d("HTTP retry-handler", "Retrying request...")
      true
    }
  }

  httpClient setHttpRequestRetryHandler retryHandler

  def getHttpClient: HttpClient = httpClient
}
