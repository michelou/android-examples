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

import android.content.Context
import android.os.Bundle
import android.support.v4.app.{Fragment, FragmentActivity}
import android.view.View
import android.widget.TabHost

import scala.collection.mutable.HashMap

/**
 * This demonstrates how you can implement switching between the tabs of a
 * TabHost through fragments.  It uses a trick (see the code below) to allow
 * the tabs to switch between fragments instead of simple views.
 */
class FragmentTabs extends FragmentActivity {
  import FragmentTabs._  // companion object

  private var mTabHost: TabHost = _
  private var mTabManager: TabManager = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_tabs)

    mTabHost = findViewById(android.R.id.tabhost).asInstanceOf[TabHost]
    mTabHost.setup()

    mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent)

    mTabManager.addTab(mTabHost.newTabSpec("simple") setIndicator "Simple",
                classOf[FragmentStackSupport.CountingFragment], null)
    mTabManager.addTab(mTabHost.newTabSpec("contacts") setIndicator "Contacts",
                classOf[LoaderCursorSupport.CursorLoaderListFragment], null)
    mTabManager.addTab(mTabHost.newTabSpec("custom") setIndicator "Custom",
                classOf[LoaderCustomSupport.AppListFragment], null)
    mTabManager.addTab(mTabHost.newTabSpec("throttle") setIndicator "Throttle",
                classOf[LoaderThrottleSupport.ThrottledLoaderListFragment], null)

    if (savedInstanceState != null) {
      mTabHost setCurrentTabByTag (savedInstanceState getString "tab")
    }
  }

  override protected def onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putString("tab", mTabHost.getCurrentTabTag)
  }

}

object FragmentTabs {

  private object TabManager {

    case class TabInfo(tag: String, clss: Class[_], args: Bundle) {
      var fragment: Fragment = _
    }

    class DummyTabFactory(context: Context) extends AnyRef with TabHost.TabContentFactory {
      override def createTabContent(tag: String): View = {
        val v = new View(context)
        v setMinimumWidth 0
        v setMinimumHeight 0
        v
      }
    }

  }

  /**
   * This is a helper class that implements a generic mechanism for
   * associating fragments with the tabs in a tab host.  It relies on a
   * trick.  Normally a tab host has a simple API for supplying a View or
   * Intent that each tab will show.  This is not sufficient for switching
   * between fragments.  So instead we make the content part of the tab host
   * 0dp high (it is not shown) and the TabManager supplies its own dummy
   * view to show as the tab content.  It listens to changes in tabs, and takes
   * care of switch to the correct fragment shown in a separate content area
   * whenever the selected tab changes.
   */
  private class TabManager(activity: FragmentActivity, tabHost: TabHost,
                          containerId: Int) extends AnyRef with TabHost.OnTabChangeListener {
    import TabManager._

    private val mFMgr = activity.getSupportFragmentManager
    private val mTabs = new HashMap[String, TabInfo]()
    private var mLastTab: TabInfo = _

    tabHost setOnTabChangedListener this

    def addTab(tabSpec: TabHost#TabSpec, clss: Class[_], args: Bundle) {
      tabSpec setContent new DummyTabFactory(activity)
      val tag = tabSpec.getTag

      val info = TabInfo(tag, clss, args)

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state.  If so, deactivate it, because our
      // initial state is that a tab isn't shown.
      info.fragment = mFMgr findFragmentByTag tag
      if (info.fragment != null && !info.fragment.isDetached) {
        val ft = mFMgr.beginTransaction()
        ft detach info.fragment
        ft.commit()
      }

      mTabs.put(tag, info)
      tabHost addTab tabSpec
    }

    override def onTabChanged(tabId: String) {
      val newTab = mTabs(tabId)
      if (mLastTab != newTab) {
        val ft = mFMgr.beginTransaction()
        if (mLastTab != null) {
          if (mLastTab.fragment != null) {
            ft detach mLastTab.fragment
          }
        }
        if (newTab != null) {
          if (newTab.fragment == null) {
            newTab.fragment = Fragment.instantiate(activity,
                                newTab.clss.getName, newTab.args)
            ft.add(containerId, newTab.fragment, newTab.tag)
          } else {
            ft attach newTab.fragment
          }
        }

        mLastTab = newTab
        ft.commit()
        mFMgr.executePendingTransactions()
      }
    }
  }
}

