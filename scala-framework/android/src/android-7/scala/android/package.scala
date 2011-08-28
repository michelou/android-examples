/*                     __                                               *\
**     ________ ___   / /  ___     Scala Android                        **
**    / __/ __// _ | / /  / _ |    (c) 2009-2011, LAMP/EPFL             **
**  __\ \/ /__/ __ |/ /__/ __ |    http://scala-lang.org/               **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */


package scala

import _root_.android.app.{Activity, Dialog}
import _root_.android.content.Context
import _root_.android.hardware.SensorManager
import _root_.android.location.LocationManager
import _root_.android.os.PowerManager
import _root_.android.telephony.TelephonyManager
import _root_.android.view.{LayoutInflater, View, ViewGroup, WindowManager}
import _root_.android.widget.{Button, EditText, ImageButton, ImageSwitcher,
                              ImageView, ListView, Spinner, TabHost,
                              TableLayout, TextSwitcher, TextView}

/**
 * @author  Stephane Micheloud
 * @version 1.1
 */
package object android {

  private[scala] trait FindView {

    protected def _findViewById[V <: View](id: Int): V

    @inline def findView[V <: View](id: Int): V =
      _findViewById(id).asInstanceOf[V]

    @inline def findButton(id: Int): Button = 
      _findViewById(id).asInstanceOf[Button]

    @inline def findEditText(id: Int): EditText =
      _findViewById(id).asInstanceOf[EditText]

    @inline def findImageButton(id: Int): ImageButton =
      _findViewById(id).asInstanceOf[ImageButton]

    @inline def findImageSwitcher(id: Int): ImageSwitcher =
      _findViewById(id).asInstanceOf[ImageSwitcher]

    @inline def findImageView(id: Int): ImageView =
      _findViewById(id).asInstanceOf[ImageView]

    @inline def findListView(id: Int): ListView =
      _findViewById(id).asInstanceOf[ListView]

    @inline def findSpinner(id: Int): Spinner =
      _findViewById(id).asInstanceOf[Spinner]

    @inline def findTabHost(id: Int): TabHost =
      _findViewById(id).asInstanceOf[TabHost]

    @inline def findTableLayout(id: Int): TableLayout =
      _findViewById(id).asInstanceOf[TableLayout]

    @inline def findTextSwitcher(id: Int): TextSwitcher =
      _findViewById(id).asInstanceOf[TextSwitcher]

    @inline def findTextView(id: Int): TextView =
      _findViewById(id).asInstanceOf[TextView]

    @inline def findViewGroup(id: Int): ViewGroup =
      _findViewById(id).asInstanceOf[ViewGroup]
  }

  implicit def findViewInActivity[A <: Activity](activity: A) = new FindView {

    @inline def _findViewById[V <: View](id: Int): V =
      activity.findViewById(id).asInstanceOf[V]

    @inline def getLayoutInflater: LayoutInflater =
      activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    @inline def getLocationManager: LocationManager =
      activity.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    @inline def getPowerManager: PowerManager =
      activity.getSystemService(POWER_SERVICE).asInstanceOf[PowerManager]

    @inline def getSensorManager: SensorManager =
      activity.getSystemService(SENSOR_SERVICE).asInstanceOf[SensorManager]

    @inline def getTelephonyManager: TelephonyManager =
      activity.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]

    @inline def getWindowManager: WindowManager =
      activity.getSystemService(WINDOW_SERVICE).asInstanceOf[WindowManager]
  }

  implicit def findViewInDialog[D <: Dialog](dialog: D) = new FindView {

    @inline def _findViewById[V <: View](id: Int): V =
      dialog.findViewById(id).asInstanceOf[V]
  }

  implicit def findViewInView[V <: View](view: V) = new FindView {

    @inline def _findViewById[V <: View](id: Int): V =
      view.findViewById(id).asInstanceOf[V]

    @inline def findButtonWithTag(tag: AnyRef): Button =
      view.findViewWithTag(tag).asInstanceOf[Button]

    @inline def findEditTextWithTag(tag: AnyRef): EditText =
      view.findViewWithTag(tag).asInstanceOf[EditText]

    @inline def findImageButtonWithTag(tag: AnyRef): ImageButton =
      view.findViewWithTag(tag).asInstanceOf[ImageButton]

    @inline def findImageSwitcherWithTag(tag: AnyRef): ImageSwitcher =
      view.findViewWithTag(tag).asInstanceOf[ImageSwitcher]

    @inline def findListViewWithTag(tag: AnyRef): ListView =
      view.findViewWithTag(tag).asInstanceOf[ListView]

    @inline def findSpinnerWithTag(tag: AnyRef): Spinner =
      view.findViewWithTag(tag).asInstanceOf[Spinner]

    @inline def findTabHostWithTag(tag: AnyRef): TabHost =
      view.findViewWithTag(tag).asInstanceOf[TabHost]

    @inline def findTableLayoutWithTag(tag: AnyRef): TableLayout =
      view.findViewWithTag(tag).asInstanceOf[TableLayout]

    @inline def findTextSwitcherWithTag(tag: AnyRef): TextSwitcher =
      view.findViewWithTag(tag).asInstanceOf[TextSwitcher]

    @inline def findTextViewWithTag(tag: AnyRef): TextView =
      view.findViewWithTag(tag).asInstanceOf[TextView]

    @inline def findViewGroupWithTag(tag: AnyRef): ViewGroup =
      view.findViewWithTag(tag).asInstanceOf[ViewGroup]
  }

}

