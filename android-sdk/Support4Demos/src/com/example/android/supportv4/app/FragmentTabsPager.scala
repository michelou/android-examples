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
import android.support.v4.app.{Fragment, FragmentActivity, FragmentPagerAdapter}
import android.support.v4.view.ViewPager
import android.view.{View, ViewGroup}
import android.widget.{TabHost, TabWidget}

import scala.collection.mutable.ListBuffer

/**
 * Demonstrates combining a TabHost with a ViewPager to implement a tab UI
 * that switches between tabs and also allows the user to perform horizontal
 * flicks to move between the tabs.
 */
class FragmentTabsPager extends FragmentActivity {
  import FragmentTabsPager._  // companion object

  private var mTabHost: TabHost = _
  private var mViewPager: ViewPager = _
  private var mTabsAdapter: TabsAdapter = _

  override protected def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.fragment_tabs_pager)

    mTabHost = findViewById(android.R.id.tabhost).asInstanceOf[TabHost]
    mTabHost.setup()

    mViewPager = findViewById(R.id.pager).asInstanceOf[ViewPager]

    mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager)

    mTabsAdapter.addTab(mTabHost.newTabSpec("simple") setIndicator "Simple",
                 classOf[FragmentStackSupport.CountingFragment], null)
    mTabsAdapter.addTab(mTabHost.newTabSpec("contacts") setIndicator "Contacts",
                 classOf[LoaderCursorSupport.CursorLoaderListFragment], null)
    mTabsAdapter.addTab(mTabHost.newTabSpec("custom") setIndicator "Custom",
                 classOf[LoaderCustomSupport.AppListFragment], null)
    mTabsAdapter.addTab(mTabHost.newTabSpec("throttle") setIndicator "Throttle",
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

object FragmentTabsPager {

  private object TabsAdapter {

    case class TabInfo(tag: String, clss: Class[_], args: Bundle)

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
   * This is a helper class that implements the management of tabs and all
   * details of connecting a ViewPager with associated TabHost.  It relies on a
   * trick.  Normally a tab host has a simple API for supplying a View or
   * Intent that each tab will show.  This is not sufficient for switching
   * between pages.  So instead we make the content part of the tab host
   * 0dp high (it is not shown) and the TabsAdapter supplies its own dummy
   * view to show as the tab content.  It listens to changes in tabs, and takes
   * care of switch to the correct paged in the ViewPager whenever the selected
   * tab changes.
   */
  private class TabsAdapter(activity: FragmentActivity, tabHost: TabHost,
                            pager: ViewPager)
        extends FragmentPagerAdapter(activity.getSupportFragmentManager)
           with TabHost.OnTabChangeListener
           with ViewPager.OnPageChangeListener {
    import TabsAdapter._  // companion object

    private val mTabs = new ListBuffer[TabInfo]()

    tabHost setOnTabChangedListener this
    pager setAdapter this
    pager setOnPageChangeListener this

    def addTab(tabSpec: TabHost#TabSpec, clss: Class[_], args: Bundle) {
      tabSpec setContent new DummyTabFactory(activity)
      val tag = tabSpec.getTag

      val info = new TabInfo(tag, clss, args)
      mTabs += info
      tabHost addTab tabSpec
      notifyDataSetChanged()
    }

    override def getCount: Int = mTabs.size

    override def getItem(position: Int): Fragment = {
      val info = mTabs(position)
      Fragment.instantiate(activity, info.clss.getName, info.args)
    }

    override def onTabChanged(tabId: String) {
      val position = tabHost.getCurrentTab
      pager setCurrentItem position
    }

    override def onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override def onPageSelected(position: Int) {
      // Unfortunately when TabHost changes the current tab, it kindly
      // also takes care of putting focus on it when not in touch mode.
      // The jerk.
      // This hack tries to prevent this from pulling focus out of our
      // ViewPager.
      val widget = tabHost.getTabWidget
      val oldFocusability = widget.getDescendantFocusability
      widget setDescendantFocusability ViewGroup.FOCUS_BLOCK_DESCENDANTS
      tabHost setCurrentTab position
      widget setDescendantFocusability oldFocusability
    }

    override def onPageScrollStateChanged(state: Int) {
    }
  }
}
