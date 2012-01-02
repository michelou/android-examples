package com.manning.aip;

import org.apache.http.HttpHost
import org.apache.http.conn.params.ConnRoutePNames
import org.apache.http.params.HttpParams

import android.content.{BroadcastReceiver, Context, Intent}
import android.net.{ConnectivityManager, NetworkInfo, Proxy}
import android.util.Log

class ConnectionChangedBroadcastReceiver extends BroadcastReceiver {

  def onReceive(context: Context, intent: Intent) {
    val info = intent getStringExtra ConnectivityManager.EXTRA_EXTRA_INFO
    val nwInfo: NetworkInfo = intent getParcelableExtra ConnectivityManager.EXTRA_NETWORK_INFO
    Log.d("Connectivity change", info + ": " + nwInfo.getReason)

    val httpParams = MyMovies.getHttpClient.getParams
    if (nwInfo.getType == ConnectivityManager.TYPE_MOBILE) {
      var proxyHost = Proxy.getHost(context);
      if (proxyHost == null) proxyHost = Proxy.getDefaultHost
      var proxyPort = Proxy.getPort(context);
      if (proxyPort == -1) proxyPort = Proxy.getDefaultPort

      if (proxyHost != null && proxyPort > -1) {
        val proxy = new HttpHost(proxyHost, proxyPort)
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy)
      } else {
        httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, null)
      }
    } else {
      httpParams.setParameter(ConnRoutePNames.DEFAULT_PROXY, null)
    }
  }

}
