package scala

import _root_.android.app.{Activity, Dialog}
import _root_.android.content.Context
import _root_.android.location.LocationManager
import _root_.android.telephony.TelephonyManager
import _root_.android.view.{LayoutInflater, View, ViewGroup}
import _root_.android.widget.{Button, EditText, ImageButton, ListView,
                              Spinner, TabHost, TableLayout, TextView}

/**
 * @author  Stephane Micheloud
 * @version 1.0
 */
package object android {

  private[scala] trait FindView[T <: { def findViewById(id: Int): View }] {
    protected val source: T

    @inline def findView[V <: View](id: Int): V =
      source.findViewById(id).asInstanceOf[V]

    @inline def findButton(id: Int): Button =
      source.findViewById(id).asInstanceOf[Button]

    @inline def findEditText(id: Int): EditText =
      source.findViewById(id).asInstanceOf[EditText]

    @inline def findImageButton(id: Int): ImageButton =
      source.findViewById(id).asInstanceOf[ImageButton]

    @inline def findListView(id: Int): ListView =
      source.findViewById(id).asInstanceOf[ListView]

    @inline def findSpinner(id: Int): Spinner =
      source.findViewById(id).asInstanceOf[Spinner]

    @inline def findTabHost(id: Int): TabHost =
      source.findViewById(id).asInstanceOf[TabHost]

    @inline def findTableLayout(id: Int): TableLayout =
      source.findViewById(id).asInstanceOf[TableLayout]

    @inline def findTextView(id: Int): TextView =
      source.findViewById(id).asInstanceOf[TextView]

    @inline def findViewGroup(id: Int): ViewGroup =
      source.findViewById(id).asInstanceOf[ViewGroup]
  }

  implicit def findViewInActivity[A <: Activity](activity: A) = new FindView[A]() {
    protected val source = activity

    @inline def getLayoutInflater: LayoutInflater =
      source.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

    @inline def getLocationManager: LocationManager =
      source.getSystemService(Context.LOCATION_SERVICE).asInstanceOf[LocationManager]

    @inline def getTelephonyManager: TelephonyManager =
      source.getSystemService(Context.TELEPHONY_SERVICE).asInstanceOf[TelephonyManager]
  }

  implicit def findViewInDialog[D <: Dialog](dialog: D) = new FindView[D]() {
    protected val source = dialog
  }

  implicit def findViewInView[V <: View](view: V) = new FindView[V]() {
    protected val source = view

    @inline def findButtonWithTag(tag: AnyRef): Button =
      source.findViewWithTag(tag).asInstanceOf[Button]

    @inline def findEditTextWithTag(tag: AnyRef): EditText =
      source.findViewWithTag(tag).asInstanceOf[EditText]

    @inline def findImageButtonWithTag(tag: AnyRef): ImageButton =
      source.findViewWithTag(tag).asInstanceOf[ImageButton]

    @inline def findListViewWithTag(tag: AnyRef): ListView =
      source.findViewWithTag(tag).asInstanceOf[ListView]

    @inline def findSpinnerWithTag(tag: AnyRef): Spinner =
      source.findViewWithTag(tag).asInstanceOf[Spinner]

    @inline def findTabHostWithTag(tag: AnyRef): TabHost =
      source.findViewWithTag(tag).asInstanceOf[TabHost]

    @inline def findTableLayoutWithTag(tag: AnyRef): TableLayout =
      source.findViewWithTag(tag).asInstanceOf[TableLayout]

    @inline def findTextViewWithTag(tag: AnyRef): TextView =
      source.findViewWithTag(tag).asInstanceOf[TextView]

    @inline def findViewGroupWithTag(tag: AnyRef): ViewGroup =
      source.findViewWithTag(tag).asInstanceOf[ViewGroup]
  }

}

