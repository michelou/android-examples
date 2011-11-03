/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.supportv4.app

import com.example.android.supportv4.R

import android.support.v4.app.{Fragment, FragmentActivity, FragmentManager}

import android.os.Bundle
import android.view.{LayoutInflater, View, ViewGroup}
import android.view.View.OnClickListener
import android.widget.{Button, ProgressBar}

/**
 * This example shows how you can use a Fragment to easily propagate state
 * (such as threads) across activity instances when an activity needs to be
 * restarted due to, for example, a configuration change.  This is a lot
 * easier than using the raw Activity.onRetainNonConfiguratinInstance() API.
 */
class FragmentRetainInstanceSupport extends FragmentActivity {
  import FragmentRetainInstanceSupport._  // companion object

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)

    // First time init, create the UI.
    if (savedInstanceState == null) {
      getSupportFragmentManager.beginTransaction().add(android.R.id.content,
                    new UiFragment()).commit()
    }
  }

}

object FragmentRetainInstanceSupport {

  /**
   * This is a fragment showing UI that will be updated from work done
   * in the retained fragment.
   */
  private class UiFragment extends Fragment {
    private var mWorkFragment: RetainedFragment = _

    override def onCreateView(inflater: LayoutInflater, container: ViewGroup,
                              savedInstanceState: Bundle): View = {
      val v = inflater.inflate(R.layout.fragment_retain_instance, container, false)

      // Watch for button clicks.
      val button = v.findViewById(R.id.restart).asInstanceOf[Button]
      button setOnClickListener new OnClickListener() {
        def onClick(v: View) { mWorkFragment.restart() }
      }

      v
    }

    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      val fm = getFragmentManager()

      // Check to see if we have retained the worker fragment.
      mWorkFragment = fm.findFragmentByTag("work").asInstanceOf[RetainedFragment]

      // If not retained (or first time running), we need to create it.
      if (mWorkFragment == null) {
         mWorkFragment = new RetainedFragment()
         // Tell it who it is working with.
         mWorkFragment.setTargetFragment(this, 0)
         fm.beginTransaction().add(mWorkFragment, "work").commit()
      }
    }

  }

  /**
   * This is the Fragment implementation that will be retained across
   * activity instances.  It represents some ongoing work, here a thread
   * we have that sits around incrementing a progress indicator.
   */
  private class RetainedFragment extends Fragment {
    private var mProgressBar: ProgressBar = _
    private var mPosition: Int = _
    private var mReady = false
    private var mQuiting = false

    /**
     * This is the thread that will do our work.  It sits in a loop running
     * the progress up until it has reached the top, then stops and waits.
     */
    private val mThread = new Thread() {
      override def run() {
        // We'll figure the real value out later.
        var max = 10000

        // This thread runs almost forever.
        while (true) {

          // Update our shared state with the UI.
          synchronized {
            // Our thread is stopped if the UI is not ready
            // or it has completed its work.
            while (!mReady || mPosition >= max) {
              if (mQuiting) return
              try wait()
              catch { case e: InterruptedException => }
            }

            // Now update the progress.  Note it is important that
            // we touch the progress bar with the lock held, so it
            // doesn't disappear on us.
            mPosition += 1
            max = mProgressBar.getMax
            mProgressBar setProgress mPosition
          }

          // Normally we would be doing some work, but put a kludge
          // here to pretend like we are.
          synchronized {
            try wait(50)
            catch { case e: InterruptedException => }
          }
        }
      } // run
    }

    /**
     * Fragment initialization.  We way we want to be retained and
     * start our thread.
     */
    override def onCreate(savedInstanceState: Bundle) {
      super.onCreate(savedInstanceState)

      // Tell the framework to try to keep this fragment around
      // during a configuration change.
      setRetainInstance(true);

      // Start up the worker thread.
      mThread.start()
    }

    /**
     * This is called when the Fragment's Activity is ready to go, after
     * its content view has been installed; it is called both after
     * the initial fragment creation and after the fragment is re-attached
     * to a new activity.
     */
    override def onActivityCreated(savedInstanceState: Bundle) {
      super.onActivityCreated(savedInstanceState)

      // Retrieve the progress bar from the target's view hierarchy.
      mProgressBar = getTargetFragment.getView.findViewById(
                    R.id.progress_horizontal).asInstanceOf[ProgressBar]

      // We are ready for our thread to go.
      mThread synchronized {
        mReady = true
        mThread.notify()
      }
    }

    /**
     * This is called when the fragment is going away.  It is NOT called
     * when the fragment is being propagated between activity instances.
     */
    override def onDestroy() {
      // Make the thread go away.
      mThread synchronized {
        mReady = false
        mQuiting = true
        mThread.notify()
      }

      super.onDestroy()
    }

    /**
     * This is called right before the fragment is detached from its
     * current activity instance.
     */
    override def onDetach() {
      // This fragment is being detached from its activity.  We need
      // to make sure its thread is not going to touch any activity
      // state after returning from this function.
      mThread synchronized {
        mProgressBar = null
        mReady = false
        mThread.notify()
      }

      super.onDetach()
    }

    /**
     * API for our UI to restart the progress thread.
     */
    def restart() {
      mThread synchronized {
        mPosition = 0
        mThread.notify()
      }
    }
  }
}
