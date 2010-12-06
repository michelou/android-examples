package com.androidbook.triviaquiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.{AdapterView, ArrayAdapter, TextView}

import scala.android.app.Activity

class QuizMenuActivity extends Activity {
  import QuizConstants._

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.menu)

    val menuList = findListView(R.id.ListView_Menu)

    val playText = getResources.getString(R.string.menu_item_play)
    val scoresText = getResources.getString(R.string.menu_item_scores)
    val settingsText = getResources.getString(R.string.menu_item_settings)
    val helpText = getResources.getString(R.string.menu_item_help)
    val items = Array(playText, scoresText, settingsText, helpText)
    val adapt = new ArrayAdapter[String](this, R.layout.menu_item, items)
    menuList setAdapter adapt

    menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

      def onItemClick(parent: AdapterView[_], itemClicked: View, position: Int, id: Long) {

        // Note: if the list was built "by hand" the id could be used.
        // As-is, though, each item has the same id

        val textView = itemClicked.asInstanceOf[TextView]
        val strText = textView.getText.toString

        if (strText equalsIgnoreCase playText) {
          // Launch the Game Activity
          startActivity(new Intent(QuizMenuActivity.this, classOf[QuizGameActivity]))
        } else if (strText equalsIgnoreCase helpText) {
          // Launch the Help Activity
          startActivity(new Intent(QuizMenuActivity.this, classOf[QuizHelpActivity]))
        } else if (strText equalsIgnoreCase settingsText) {
          // Launch the Settings Activity
          startActivity(new Intent(QuizMenuActivity.this, classOf[QuizSettingsActivity]))
        } else if (strText equalsIgnoreCase scoresText) {
          // Launch the Scores Activity
          startActivity(new Intent(QuizMenuActivity.this, classOf[QuizScoresActivity]))
        }

      }
    })

  }

}
