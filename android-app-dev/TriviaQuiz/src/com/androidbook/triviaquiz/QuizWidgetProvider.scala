package com.androidbook.triviaquiz

import java.io.IOException
import java.net.{MalformedURLException, URL}

import org.xmlpull.v1.{XmlPullParser, XmlPullParserException, XmlPullParserFactory}

import android.app.{PendingIntent, Service}
import android.appwidget.{AppWidgetManager, AppWidgetProvider}
import android.content.{ComponentName, Context, Intent, SharedPreferences}
import android.graphics.{Bitmap, BitmapFactory}
import android.os.IBinder
import android.util.Log
import android.widget.RemoteViews

class QuizWidgetProvider extends AppWidgetProvider {
  import QuizWidgetProvider._  // companion object

  // private static final int IO_BUFFER_SIZE = 4 * 1024;

  override def onUpdate(context: Context, appWidgetManager: AppWidgetManager,
                                          appWidgetIds: Array[Int]) {
    // push this to a Service so it runs in the background
    // We can't use a thread because the Provider may not remain around
    // (Don't forget to add the service entry to the Manifest)

    val serviceIntent = new Intent(context, classOf[WidgetUpdateService])
    context startService serviceIntent
  }

  override def onDeleted(context: Context, appWidgetIds: Array[Int]) {
    // Note: Ignoring the appWidgetids is safe, but could stop an update for
    // instance of this app widget if more than one is running. This widget
    // is not designed to be a multi-instance widget.
    val serviceIntent = new Intent(context, classOf[WidgetUpdateService])
    context stopService serviceIntent

    super.onDeleted(context, appWidgetIds)
  }

}

object QuizWidgetProvider {

  object WidgetUpdateService {
    private final val DEBUG_TAG = "WidgetUpdateService"
  }

  class WidgetUpdateService extends Service {
    import WidgetUpdateService._  // companion object

    var widgetUpdateThread: Thread = _

    override def onStartCommand(intent: Intent, flags: Int, startId: Int): Int = {
      widgetUpdateThread = new Thread() {
        override def run() {
          val context = WidgetUpdateService.this
          val widgetData = new WidgetData("Unknown", "NA", "")

          getWidgetData(widgetData)

          // prep the RemoteView
          val packageName = context.getPackageName
          Log.d(DEBUG_TAG, "packageName: " + packageName)
          val remoteView = new RemoteViews(packageName, R.layout.widget)
          remoteView.setTextViewText(R.id.widget_nickname, widgetData.nickname)
          remoteView.setTextViewText(R.id.widget_score, "Score: " + widgetData.score)
          if (widgetData.avatarUrl.length > 0) {
            // remoteView.setImageViewUri(R.id.widget_image, Uri.parse(avatarUrl))
            var image: URL = null
            try {
              image = new URL(widgetData.avatarUrl)
              Log.d(DEBUG_TAG, "avatarUrl: " + widgetData.avatarUrl)

              // See http://bit.ly/bAtW6W and http://bit.ly/a3Qkw4 for the reasons
              // for not using decodeStream directly
              // (in short, it works but not in certain situations)
              // The work around shown below was also used in Android Wireless Application Development.

              val bitmap = BitmapFactory.decodeStream(image.openStream())
              /*
               * BufferedInputStream in; BufferedOutputStream out;
               * 
               * in = new BufferedInputStream(image.openStream(), IO_BUFFER_SIZE);
               * final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
               * out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
               * copy(in, out); // implementation provided at the bottom of this file; uncomment to use out.flush();
               * 
               * final byte[] data = dataStream.toByteArray();
               * Log.d(DEBUG_TAG, "Length: "+ data.length);
               * Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
              */
              if (bitmap == null) {
                Log.w(DEBUG_TAG, "Failed to decode image")

                remoteView.setImageViewResource(R.id.widget_image, R.drawable.avatar)
              } else {
                remoteView.setImageViewBitmap(R.id.widget_image, bitmap)
              }
            } catch {
              case e: MalformedURLException =>
                Log.e(DEBUG_TAG, "Bad url in image", e)
              case e: IOException =>
                Log.e(DEBUG_TAG, "IO failure for image", e)
            }

          } else {
            remoteView.setImageViewResource(R.id.widget_image, R.drawable.avatar)
          }

          try {
            // add click handling
            val launchAppIntent = new Intent(context, classOf[QuizMenuActivity])
            val launchAppPendingIntent = PendingIntent.getActivity(context, 0, launchAppIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            remoteView.setOnClickPendingIntent(R.id.widget_view, launchAppPendingIntent)

            // get the Android component name for the QuizWidgetProvider
            val quizWidget = new ComponentName(context, classOf[QuizWidgetProvider])

            // get the instance of the AppWidgetManager
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // update the widget
            appWidgetManager.updateAppWidget(quizWidget, remoteView)

          } catch {
            case e: Exception =>
              Log.e(DEBUG_TAG, "Failed to update widget", e)
          }

          if (!WidgetUpdateService.this.stopSelfResult(startId)) {
            Log.e(DEBUG_TAG, "Failed to stop service")
          }
        } // def run

        /**
         * Download data for displaying in the Widget
         * 
         * @param widgetData
         */
        private def getWidgetData(widgetData: WidgetData) {
          val prefs = getSharedPreferences(QuizConstants.GAME_PREFERENCES, Context.MODE_PRIVATE)
          val playerId = prefs.getInt(QuizConstants.GAME_PREFERENCES_PLAYER_ID, -1)

          try {
            val userInfo = new URL(QuizConstants.TRIVIA_SERVER_BASE + "getplayer?playerId=" + playerId)
            val parser = XmlPullParserFactory.newInstance().newPullParser()
            parser.setInput(userInfo.openStream(), null)

            var eventType = -1
            while (eventType != XmlPullParser.END_DOCUMENT) {
              if (eventType == XmlPullParser.START_TAG) {
                val strName = parser.getName

                if (strName equals "nickname")
                  widgetData.nickname = parser.nextText
                else if (strName equals "score")
                  widgetData.score = parser.nextText
                else if (strName equals "avatarUrl")
                  widgetData.avatarUrl = parser.nextText
              }
              eventType = parser.next()
            } // while
          } catch {
            case e: MalformedURLException =>
              Log.e(DEBUG_TAG, "Bad URL", e)
            case e: XmlPullParserException =>
              Log.e(DEBUG_TAG, "Parser exception", e)
            case e: IOException =>
              Log.e(DEBUG_TAG, "IO Exception", e)
          }
        }
      } // new Thread

      // start the background thread
      widgetUpdateThread.start()

      // if we're killed, restart us with the original Intent so we get
      // an extra data again, should we choose to use it later
      Service.START_REDELIVER_INTENT
    } // def onStartCommand

    override def onDestroy() {
      widgetUpdateThread.interrupt()
      super.onDestroy()
    }

    override def onBind(intent: Intent): IBinder = {
      // no binding; can't from an App Widget
      null
    }

    /**
     * Copy the content of the input stream into the output stream, using
     * a temporary byte array buffer whose size is defined by {@link #IO_BUFFER_SIZE}.
     *
     * @param in
     *            The input stream to copy from.
     * @param out
     *            The output stream to copy to.
     * @throws IOException
     *             If any error occurs during the copy.
     */
    /*
     * private static void copy(InputStream in, OutputStream out) throws IOException {
     *     byte[] b = new byte[IO_BUFFER_SIZE];
     *     int read;
     *     while ((read = in.read(b)) != -1) { out.write(b, 0, read); }
     * }
     */

    class WidgetData(var nickname: String, var score: String, var avatarUrl: String)
  }

}
