package be.whaxion.timeatwork.utils

import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

const val SECONDS_IN_ONE_DAY = 86400L

class TimeUtils {
    companion object {
        val dateFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("EE d")
        val timeFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("HH'h'mm")
        val dayMonthFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("dd/M")
        val monthTitleFormatter : DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM")

        fun daysOfWeekFromLocale(): Array<DayOfWeek> {
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val daysOfWeek = DayOfWeek.values()
            if (firstDayOfWeek != DayOfWeek.MONDAY) {
                val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
                val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
                return rhs + lhs
            }
            return daysOfWeek
        }

        fun timeToString(time : Long) : String {
            var timeMinutes = (time % 3600 / 60).toString()
            var timeHours = (time / 3600).toString()
            if(timeMinutes.length == 1)
                timeMinutes = "0$timeMinutes"
            if(timeHours.length == 1)
                timeHours = "0$timeHours"

            return "${timeHours}h$timeMinutes"
        }
    }
}