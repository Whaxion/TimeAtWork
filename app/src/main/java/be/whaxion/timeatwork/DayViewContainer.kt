package be.whaxion.timeatwork

import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import be.whaxion.timeatwork.fragment.TimeEntryCreateFragment
import com.kizitonwose.calendarview.ui.ViewContainer

class DayViewContainer(view: View) : ViewContainer(view) {
    val container = view.findViewById<ConstraintLayout>(R.id.calendar_day_container)
    val dayTextView = view.findViewById<TextView>(R.id.calendar_day_text)
    val timeSpentTextView = view.findViewById<TextView>(R.id.calendar_time_spent)
}