package be.whaxion.timeatwork

import android.view.View
import android.widget.LinearLayout
import com.kizitonwose.calendarview.ui.ViewContainer

class MonthViewContainer(view: View) : ViewContainer(view) {
    val legendLayout = view.findViewById<LinearLayout>(R.id.legendLayout)
}