package com.androidbook.triviaquiz

import java.io.IOException
import java.net.URL

import org.xmlpull.v1.{XmlPullParser, XmlPullParserException, XmlPullParserFactory}

import android.content.{Context, SharedPreferences}
import android.content.res.XmlResourceParser
import android.os.{AsyncTask, Bundle}
import android.util.Log
import android.view.Window
import android.widget.{TableLayout, TableRow, TextView}

import scala.android.app.Activity

class QuizScoresActivity extends Activity {
  import QuizConstants._

  private var mProgressCounter = 0
  private var allScoresDownloader: ScoreDownloaderTask = _
  private var friendScoresDownloader: ScoreDownloaderTask = _

  /** Called when the activity is first created. */

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS)
    setContentView(R.layout.scores)

    // Set up the tabs
    val host = findTabHost(R.id.TabHost1)
    host.setup()

    // All Scores tab
    val allScoresTab = host newTabSpec "allTab"
    allScoresTab.setIndicator(getResources.getString(R.string.all_scores), getResources.getDrawable(android.R.drawable.star_on))
    allScoresTab setContent R.id.ScrollViewAllScores
    host addTab allScoresTab

    // Friends Scores tab
    val friendScoresTab = host newTabSpec "friendsTab"
    friendScoresTab.setIndicator(getResources.getString(R.string.friends_scores), getResources.getDrawable(android.R.drawable.star_on))
    friendScoresTab setContent R.id.ScrollViewFriendScores
    host addTab friendScoresTab

    // Set the default tab
    host setCurrentTabByTag "allTab"

    // Retrieve the TableLayout references
    val allScoresTable = findTableLayout(R.id.TableLayout_AllScores)
    val friendScoresTable = findTableLayout(R.id.TableLayout_FriendScores)

    // Give each TableLayout a yellow header row with the column names
    initializeHeaderRow(allScoresTable)
    initializeHeaderRow(friendScoresTable)

    allScoresDownloader = new ScoreDownloaderTask()
    allScoresDownloader.execute(TRIVIA_SERVER_SCORES, allScoresTable)

    val prefs = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE)
    val playerId = prefs.getInt(GAME_PREFERENCES_PLAYER_ID, -1)

    if (playerId != -1) {
      friendScoresDownloader = new ScoreDownloaderTask()
      friendScoresDownloader.execute(
        TRIVIA_SERVER_SCORES + "?playerId=" + playerId, friendScoresTable)
    }
  }

  override protected def onPause() {
    if (allScoresDownloader != null &&
        allScoresDownloader.getStatus != AsyncTask.Status.FINISHED) {
      allScoresDownloader cancel true
    }
    if (friendScoresDownloader != null &&
        friendScoresDownloader.getStatus != AsyncTask.Status.FINISHED) {
      friendScoresDownloader cancel true
    }
    super.onPause()
  }

  /**
   * 
   * Add a header {@code TableRow} to the {@code TableLayout} (styled)
   * 
   * @param scoreTable
   *            the {@code TableLayout} that the header row will be added to
   */
  private def initializeHeaderRow(scoreTable: TableLayout) {
    // Create the Table header row
    val headerRow = new TableRow(this)
    val rsrc = getResources

    val textColor = rsrc getColor R.color.logo_color
    val textSize = rsrc getDimension R.dimen.help_text_size

    addTextToRowWithValues(headerRow, rsrc getString R.string.username, textColor, textSize)
    addTextToRowWithValues(headerRow, rsrc getString R.string.score, textColor, textSize)
    addTextToRowWithValues(headerRow, rsrc getString R.string.rank, textColor, textSize)
    scoreTable addView headerRow
  }

  /**
   * {@code insertScoreRow()} helper method -- Populate a {@code TableRow}
   * with three columns of {@code TextView} data (styled)
   * 
   * @param tableRow
   *            The {@code TableRow} the text is being added to
   * @param text
   *            The text to add
   * @param textColor
   *            The color to make the text
   * @param textSize
   *            The size to make the text
   */
  private def addTextToRowWithValues(tableRow: TableRow, text: String,
                                     textColor: Int, textSize: Float) {
    val textView = new TextView(this)
    textView setTextSize textSize
    textView setTextColor textColor
    textView setText text
    tableRow addView textView
  }

  private object ScoreDownloaderTask {
    private final val DEBUG_TAG = "ScoreDownloaderTask"
  }

  private class ScoreDownloaderTask extends AsyncTask[AnyRef, String, Boolean] {
    import ScoreDownloaderTask._  // companion object

    var table: TableLayout = _

    override protected def onCancelled() {
      Log.i(DEBUG_TAG, "onCancelled")
      mProgressCounter -= 1
      if (mProgressCounter <= 0) {
        mProgressCounter = 0
        QuizScoresActivity.this setProgressBarIndeterminateVisibility false
      }
    }

    override protected def onPostExecute(result: Boolean) {
      Log.i(DEBUG_TAG, "onPostExecute")
      mProgressCounter -= 1
      if (mProgressCounter <= 0) {
        mProgressCounter = 0
        QuizScoresActivity.this setProgressBarIndeterminateVisibility false
      }
    }

    override protected def onPreExecute() {
      mProgressCounter += 1
      QuizScoresActivity.this setProgressBarIndeterminateVisibility true
    }

    override protected def onProgressUpdate(values: String*) {
      if (values.length == 3) {
        val scoreValue = values(0)
        val scoreRank = values(1)
        val scoreUserName = values(2)
        insertScoreRow(table, scoreValue, scoreRank, scoreUserName)
      } else {
        val newRow = new TableRow(QuizScoresActivity.this)
        val noResults = new TextView(QuizScoresActivity.this)
        noResults setText getResources.getString(R.string.no_scores)
        newRow addView noResults
        table addView newRow
      }

    }

    override protected def doInBackground(params: AnyRef*): Boolean = {
      var result = false
      val pathToScores = params(0).asInstanceOf[String]
      val table = params(1).asInstanceOf[TableLayout]

      var scores: XmlPullParser = null
      try {
        val xmlUrl = new URL(pathToScores)
        scores = XmlPullParserFactory.newInstance().newPullParser()
        scores.setInput(xmlUrl.openStream(), null)
      } catch {
        case e: XmlPullParserException =>
          scores = null
        case e: IOException =>
          scores = null
      }

      if (scores != null) {
        try {
          processScores(scores)
        } catch {
          case e: XmlPullParserException =>
            Log.e(DEBUG_TAG, "Pull Parser failure", e)
          case e: IOException =>
            Log.e(DEBUG_TAG, "IO Exception parsing XML", e)
        }
      }

      result
    }

    /**
     * 
     * {@code processScores()} helper method -- Inserts a new score
     * {@code TableRow} in the {@code TableLayout}
     * 
     * @param scoreTable
     *            The {@code TableLayout} to add the score to
     * @param scoreValue
     *            The value of the score
     * @param scoreRank
     *            The ranking of the score
     * @param scoreUserName
     *            The user who made the score
     */
    private def insertScoreRow(scoreTable: TableLayout, scoreValue: String,
                               scoreRank: String, scoreUserName: String) {
      val newRow = new TableRow(QuizScoresActivity.this)

      val textColor = getResources getColor R.color.title_color
      val textSize = getResources getDimension R.dimen.help_text_size

      addTextToRowWithValues(newRow, scoreUserName, textColor, textSize)
      addTextToRowWithValues(newRow, scoreValue, textColor, textSize)
      addTextToRowWithValues(newRow, scoreRank, textColor, textSize)
      scoreTable addView newRow
    }

    /**
     * Churn through an XML score information and populate a {@code TableLayout}
     * 
     * @param scores
     *            A standard {@code XmlPullParser} containing the scores
     * @throws XmlPullParserException
     *             Thrown on XML errors
     * @throws IOException
     *             Thrown on IO errors reading the XML
     */
    @throws(classOf[XmlPullParserException])
    @throws(classOf[IOException])
    private def processScores(scores: XmlPullParser) {
      var eventType = -1
      var bFoundScores = false

      // Find Score records from XML
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {

          // Get the name of the tag (eg scores or score)
          val strName = scores.getName

          if (strName equals "score") {
            bFoundScores = true
            val scoreValue = scores.getAttributeValue(null, "score")
            val scoreRank = scores.getAttributeValue(null, "rank")
            val scoreUserName = scores.getAttributeValue(null, "username")
            publishProgress(scoreValue, scoreRank, scoreUserName)
          }
        }
        eventType = scores.next()
      }

      // Handle no scores available
      if (!bFoundScores) {
        publishProgress()
      }
    }

  }

}
