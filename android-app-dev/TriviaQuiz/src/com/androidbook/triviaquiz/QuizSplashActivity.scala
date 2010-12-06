package com.androidbook.triviaquiz

import android.content.Intent
import android.os.Bundle
import android.view.animation.{Animation, AnimationUtils, LayoutAnimationController}
import android.view.animation.Animation.AnimationListener
import android.widget.{TableLayout, TableRow, TextView}

import scala.android.app.Activity

class QuizSplashActivity extends Activity {
  import QuizConstants._

  /** Called when the activity is first created. */
  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.splash)
    startAnimating()
  }

  /**
   * Helper method to start the animation on the splash screen
   */
  private def startAnimating() {
    // Fade in top title
    val logo1 = findTextView(R.id.TextViewTopTitle)
    val fade1 = AnimationUtils.loadAnimation(this, R.anim.fade_in)
    logo1 startAnimation fade1

    // Fade in bottom title after a built-in delay.
    val logo2 = findTextView(R.id.TextViewBottomTitle)
    val fade2 = AnimationUtils.loadAnimation(this, R.anim.fade_in2)
    logo2 startAnimation fade2

    // Transition to Main Menu when bottom title finishes animating
    fade2 setAnimationListener new AnimationListener() {
      def onAnimationEnd(animation: Animation) {
        // The animation has ended, transition to the Main Menu screen
        startActivity(new Intent(QuizSplashActivity.this, classOf[QuizMenuActivity]))
        QuizSplashActivity.this.finish()
      }
      def onAnimationRepeat(animation: Animation) {}
      def onAnimationStart(animation: Animation) {}
    }

    // Load animations for all views within the TableLayout
    val spinin = AnimationUtils.loadAnimation(this, R.anim.custom_anim)
    val controller = new LayoutAnimationController(spinin)

    val table = findTableLayout(R.id.TableLayout01)
    for (i <- 0 until table.getChildCount) {
      val row = table.getChildAt(i).asInstanceOf[TableRow]
      row setLayoutAnimation controller
    }

  }

  override protected def onPause() {
    super.onPause()
    // Stop the animation
    val logo1 = findTextView(R.id.TextViewTopTitle)
    logo1.clearAnimation()

    val logo2 = findTextView(R.id.TextViewBottomTitle)
    logo2.clearAnimation()

    val table = findTableLayout(R.id.TableLayout01)
    for (i <- 0 until table.getChildCount) {
      val row = table.getChildAt(i).asInstanceOf[TableRow]
      row.clearAnimation()
    }
  }

  override protected def onResume() {
    super.onResume()

    // Start animating at the beginning so we get the full splash screen experience
    startAnimating()
  }

}
