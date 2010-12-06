package com.androidbook.triviaquiz

import java.io.IOException
import java.net.URL

import org.xmlpull.v1.{XmlPullParser, XmlPullParserException, XmlPullParserFactory}

import android.app.ProgressDialog
import android.content.{Context, DialogInterface, Intent, SharedPreferences}
import android.content.DialogInterface.OnCancelListener
import android.content.SharedPreferences.Editor
import android.content.res.{Resources, XmlResourceParser}
import android.graphics.{Bitmap, BitmapFactory}
import android.graphics.drawable.{BitmapDrawable, Drawable}
import android.os.{AsyncTask, Bundle}
import android.util.Log
import android.view.{Gravity, Menu, MenuItem, View}
import android.view.ViewGroup.LayoutParams
import android.view.animation.{Animation, AnimationUtils}
import android.widget.{Button, ImageSwitcher, ImageView, TextSwitcher,
                       TextView, ViewSwitcher}

import scala.android.app.Activity
import scala.collection.mutable.HashMap

class QuizGameActivity extends Activity {
  import QuizConstants._

  /** Called when the activity is first created. */

  private var mGameSettings: SharedPreferences = _
  private var mQuestions: HashMap[Int, Question] = _
  private var mQuestionText: TextSwitcher = _
  private var mQuestionImage: ImageSwitcher = _
  private var downloader: QuizTask = _

  override protected def onPause() {
    if (downloader != null && downloader.getStatus != AsyncTask.Status.FINISHED) {
      downloader cancel true
    }
    super.onPause()
  }

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.game)

    // Retrieve the shared preferences
    mGameSettings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE)

    // Initialize question batch
    mQuestions = new HashMap[Int, Question] //(QUESTION_BATCH_SIZE)

    // Get our progress through the questions
    var startingQuestionNumber = mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 0)

    // If we're at the beginning of the quiz, initialize the Shared preferences
    if (startingQuestionNumber == 0) {
      startingQuestionNumber = 1
      val editor = mGameSettings.edit
      editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, startingQuestionNumber)
      editor.commit()
    }

    // Start loading the questions in the background
    downloader = new QuizTask()
    downloader.execute(TRIVIA_SERVER_QUESTIONS, startingQuestionNumber.asInstanceOf[AnyRef])

    // in the meantime, configure UI
    // Handle yes button
    val yesButton = findButton(R.id.Button_Yes)
    yesButton setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        handleAnswerAndShowNextQuestion(true)
      }
    }

    // Handle no button
    val noButton = findButton(R.id.Button_No)
    noButton setOnClickListener new View.OnClickListener() {
      def onClick(v: View) {
        handleAnswerAndShowNextQuestion(false)
      }
    }

    // Set up Text Switcher
    val in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
    val out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)

    mQuestionText = findTextSwitcher(R.id.TextSwitcher_QuestionText)
    mQuestionText setInAnimation in
    mQuestionText setOutAnimation out
    mQuestionText setFactory new MyTextSwitcherFactory()

    mQuestionImage = findImageSwitcher(R.id.ImageSwitcher_QuestionImage)
    mQuestionImage setInAnimation in
    mQuestionImage setOutAnimation out
    mQuestionImage setFactory new MyImageSwitcherFactory()
  }

  /**
   * Called when question loading is complete
   * 
   * @param startingQuestionNumber
   *            The first question number that should be available
   */
  private def displayCurrentQuestion(startingQuestionNumber: Int) {
    // If the question was loaded properly, display it
    if (mQuestions contains startingQuestionNumber) {
      // Set the text of the textswitcher
      mQuestionText setCurrentText getQuestionText(startingQuestionNumber)

      // Set the image of the imageswitcher
      val image = getQuestionImageDrawable(startingQuestionNumber)
      mQuestionImage setImageDrawable image
    } else {
      // Tell the user we don't have any new questions at this time
      handleNoQuestions();
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    super.onCreateOptionsMenu(menu)

    getMenuInflater().inflate(R.menu.gameoptions, menu)
    menu.findItem(R.id.help_menu_item).setIntent(new Intent(this, classOf[QuizHelpActivity]))
    menu.findItem(R.id.settings_menu_item).setIntent(new Intent(this, classOf[QuizSettingsActivity]))
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    super.onOptionsItemSelected(item)
    startActivity(item.getIntent)
    true
  }

  /**
   * A switcher factory for use with the question image.
   * Creates the next {@code ImageView} object to animate to
   * 
   */
  private class MyImageSwitcherFactory extends ViewSwitcher.ViewFactory {
    def makeView(): View = {
      val imageView = new ImageView(QuizGameActivity.this)
      imageView setScaleType ImageView.ScaleType.FIT_CENTER
      val params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT)
      imageView setLayoutParams params
      imageView
    }
  }

  /**
   * A switcher factory for use with the question text.
   * Creates the next {@code TextView} object to animate to
   * 
   */
  private class MyTextSwitcherFactory extends ViewSwitcher.ViewFactory {
    def makeView(): View = {
      val textView = new TextView(QuizGameActivity.this)
      textView setGravity Gravity.CENTER
      val res = getResources
      val dimension = res getDimension R.dimen.game_question_size
      val titleColor = res getColor R.color.title_color
      val shadowColor = res getColor R.color.title_glow
      textView setTextSize dimension
      textView setTextColor titleColor
      textView.setShadowLayer(10, 5, 5, shadowColor)
      textView
    }
  }

  /**
   * 
   * Helper method to record the answer the user gave and load up the next question.
   * 
   * @param bAnswer
   *            The answer the user gave
   */
  private def handleAnswerAndShowNextQuestion(bAnswer: Boolean) {
    val curScore = mGameSettings.getInt(GAME_PREFERENCES_SCORE, 0)
    val nextQuestionNumber = mGameSettings.getInt(GAME_PREFERENCES_CURRENT_QUESTION, 1) + 1

    val editor = mGameSettings.edit
    editor.putInt(GAME_PREFERENCES_CURRENT_QUESTION, nextQuestionNumber)

    // Log the number of "yes" answers only
    if (bAnswer) {
      editor.putInt(GAME_PREFERENCES_SCORE, curScore + 1)
    }
    editor.commit()

    if (!mQuestions.contains(nextQuestionNumber)) {

      downloader = new QuizTask()
      downloader.execute(TRIVIA_SERVER_QUESTIONS, nextQuestionNumber.asInstanceOf[AnyRef])

      // current question display gets deferred until this is done
    } else {

      displayCurrentQuestion(nextQuestionNumber)
    }
  }

  /**
   * Helper method to configure the question screen when no questions
   * were found. Could be called for a variety of error cases, including
   * no new questions, IO failures, or parser failures.
   */
  private def handleNoQuestions() {
    val questionTextSwitcher = findViewById(R.id.TextSwitcher_QuestionText).asInstanceOf[TextSwitcher]
    questionTextSwitcher setText getResources.getText(R.string.no_questions)
    val questionImageSwitcher = findImageSwitcher(R.id.ImageSwitcher_QuestionImage)
    questionImageSwitcher setImageResource R.drawable.noquestion

    // Disable yes button
    val yesButton = findButton(R.id.Button_Yes)
    yesButton setEnabled false

    // Disable no button
    val noButton = findButton(R.id.Button_No)
    noButton setEnabled false
  }

  /**
   * Returns a {@code String} representing the text for a particular question number
   * 
   * @param questionNumber
   *            The question number to get the text for
   * @return The text of the question, or null if {@code questionNumber} not found
   */
  private def getQuestionText(questionNumber: Int): String = {
    val curQuestion = mQuestions(questionNumber)
    if (curQuestion != null) curQuestion.mText else null
  }

  /**
   * Returns a {@code String} representing the URL to an image for a particular question
   * 
   * @param questionNumber
   *            The question to get the URL for
   * @return A {@code String} for the URL or null if none found
   */
  private def getQuestionImageUrl(questionNumber: Int): String = {
    val curQuestion = mQuestions(questionNumber)
    if (curQuestion != null) curQuestion.mImageUrl else null
  }

  /**
   * Retrieves a {@code Drawable} object for a particular question
   * 
   * @param questionNumber
   *            The question number to get the {@code Drawable} for
   * @return A {@code Drawable} for the particular question, or a placeholder
   *           image if the loading failed or the question doesn't exist
   */
  private def getQuestionImageDrawable(questionNumber: Int): Drawable =
    try {
      // Create a Drawable by decoding a stream from a remote URL
      val imageUrl = new URL(getQuestionImageUrl(questionNumber))
      val bitmap = BitmapFactory decodeStream imageUrl.openStream()
      new BitmapDrawable(bitmap)
    } catch {
      case e: Exception =>
        Log.e(DEBUG_TAG, "Decoding Bitmap stream failed.")
        getResources getDrawable R.drawable.noquestion
    }

  /**
   * Object to manage the data for a single quiz question
   * 
   */
  private case class Question(mNumber: Int, mText: String, mImageUrl: String)

  private object QuizTask {
    private final val DEBUG_TAG = "QuizGameActivity$QuizTask"
  }

  private class QuizTask extends AsyncTask[AnyRef, String, Boolean] {
    import QuizTask._  // companion object

    var startingNumber: Int = _
    var pleaseWaitDialog: ProgressDialog = _

    override protected def onCancelled() {
      Log.i(DEBUG_TAG, "onCancelled")
      handleNoQuestions()
      pleaseWaitDialog.dismiss()
    }

    override protected def onPostExecute(result: Boolean) {
      Log.d(DEBUG_TAG, "Download task complete.")
      if (result) {
        displayCurrentQuestion(startingNumber)
      } else {
        handleNoQuestions()
      }

      pleaseWaitDialog.dismiss()
    }

    override protected def onPreExecute() {
      pleaseWaitDialog = ProgressDialog.show(QuizGameActivity.this, "Trivia Quiz", "Downloading trivia questions", true, true)
      pleaseWaitDialog setOnCancelListener new OnCancelListener() {
        def onCancel(dialog: DialogInterface) {
          QuizTask.this cancel true
        }
      }
    }

    override protected def onProgressUpdate(values: String *) {
      super.onProgressUpdate(values: _*)
    }

    override protected def doInBackground(params: AnyRef*): Boolean = {
      var result = false
      try {
        // must put paramters in correct order and correct type,
        // otherwise a ClassCastException will be thrown
        startingNumber = params(1).asInstanceOf[Int]
        var pathToQuestions = params(0) + "?max=" + QUESTION_BATCH_SIZE +
                                          "&start=" + startingNumber

        // update score if account is registered
        // we do this in the same request to reduce latency and increase network efficiency
        val settings = getSharedPreferences(GAME_PREFERENCES, Context.MODE_PRIVATE)

        val playerId = settings.getInt(GAME_PREFERENCES_PLAYER_ID, -1)
        if (playerId != -1) {
          Log.d(DEBUG_TAG, "Updating score")
          val score = settings.getInt(GAME_PREFERENCES_SCORE, -1)
          if (score != -1) {
            pathToQuestions += "&updateScore=yes&updateId=" + playerId + "&score=" + score
          }
        }

        Log.d(DEBUG_TAG, "path: " + pathToQuestions + " -- Num: " + startingNumber)

        result = loadQuestionBatch(startingNumber, pathToQuestions)

      } catch {
        case e: Exception =>
          Log.e(DEBUG_TAG, "Unexpected failure in XML downloading and parsing", e);
      }

      result
    }

    /**
     * Parses the XML questions to {@see mQuestions}. They're preloaded
     * into an XmlPullParser (questionBatch)
     * 
     * @param questionBatch
     *            The incoming XmlPullParser
     * @throws XmlPullParserException
     *             Thrown if XML parsing errors
     * @throws IOException
     *             Thrown if IO exceptions
     */
    @throws(classOf[XmlPullParserException])
    @throws(classOf[IOException])
    private def parseXMLQuestionBatch(questionBatch: XmlPullParser) {
      var eventType = -1

      // Find Score records from XML
      while (eventType != XmlPullParser.END_DOCUMENT) {
        if (eventType == XmlPullParser.START_TAG) {

          // Get the name of the tag (eg questions or question)
          val strName = questionBatch.getName

          if (strName equals XML_TAG_QUESTION) {
            val questionNum = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_NUMBER).toInt
            val questionText = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_TEXT)
            val questionImageUrl = questionBatch.getAttributeValue(null, XML_TAG_QUESTION_ATTRIBUTE_IMAGEURL)

            // Save data to our hashtable
            mQuestions += questionNum -> new Question(questionNum, questionText, questionImageUrl)
          }
        }
        eventType = questionBatch.next()
      }
    }

    /**
     * Loads the XML into the {@see mQuestions} class member variable
     * 
     * @param startQuestionNumber
     *            first question to load
     */
    private def loadQuestionBatch(startQuestionNumber: Int, xmlSource: String): Boolean = {
      var result = false
      // Remove old batch
      mQuestions.clear()

      // Contact the server and retrieve a batch of question data,
      // beginning at startQuestionNumber
      var questionBatch: XmlPullParser = null
      try {
        val xmlUrl = new URL(xmlSource)
        questionBatch = XmlPullParserFactory.newInstance().newPullParser()
        questionBatch.setInput(xmlUrl.openStream(), null)
      } catch {
        case e: XmlPullParserException =>
          questionBatch = null
          Log.e(DEBUG_TAG, "Failed to initialize pull parser", e)
        case e: IOException =>
          questionBatch = null
          Log.e(DEBUG_TAG, "IO Failure during pull parser initialization", e)
      }

      // Parse the XML
      if (questionBatch != null) {
        try {
          parseXMLQuestionBatch(questionBatch)
          result = true
        } catch {
          case e: XmlPullParserException =>
            Log.e(DEBUG_TAG, "Pull Parser failure", e)
          case e: IOException =>
            Log.e(DEBUG_TAG, "IO Exception parsing XML", e)
        }
      }
      result
    }
  } // QuizTask

}
