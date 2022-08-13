package be.whaxion.timeatwork.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import be.whaxion.timeatwork.*
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.database.TimeEntry
import be.whaxion.timeatwork.database.TimeEntryDao
import be.whaxion.timeatwork.utils.SECONDS_IN_ONE_DAY
import be.whaxion.timeatwork.utils.TimeUtils
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

private const val ARG_YEAR = "ARG_YEAR"
private const val ARG_MONTH = "ARG_MONTH"

class CalendarFragment : HasMonth() {
    private var yearMonth: YearMonth = YearMonth.now()
    private val today = LocalDate.now()
    private var calendarView : CalendarView? = null
    private val liveDatas : ArrayList<LiveData<List<TimeEntry>>> = arrayListOf()

    private lateinit var mainActivity: MainActivity
    private lateinit var timeEntryDao : TimeEntryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            yearMonth = YearMonth.of(it.getInt(ARG_YEAR), it.getInt(ARG_MONTH))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        timeEntryDao = AppDatabase.getDatabase(mainActivity).timeEntryDao()

        val timesRV = view.findViewById<RecyclerView>(R.id.recyclerview_times)
        val timesAdapter = TimeAdapter(mainActivity)

        timesRV.layoutManager = LinearLayoutManager(mainActivity)
        timesRV.adapter = timesAdapter

        val daysOfWeek = TimeUtils.daysOfWeekFromLocale()
        calendarView = view.findViewById(R.id.calendarview)
        calendarView?.let { cv ->
            val totalTimeTextView = view.findViewById<TextView>(R.id.total_time)
            val currentSalaryTextView = view.findViewById<TextView>(R.id.current_salary)

            cv.dayBinder = object : DayBinder<DayViewContainer> {
                // Called only when a new container is needed.
                override fun create(view: View) = DayViewContainer(view)

                // Called every time we need to reuse a container.
                override fun bind(container: DayViewContainer, day: CalendarDay) {
                    container.dayTextView.text = day.date.dayOfMonth.toString()
                    if(day.owner == DayOwner.THIS_MONTH){
                        val epoch = day.date.toEpochDay() * SECONDS_IN_ONE_DAY
                        val data = timeEntryDao.getAllBetween(epoch, epoch + SECONDS_IN_ONE_DAY)
                        data.observe(mainActivity, { timeEntries : List<TimeEntry> ->
                            container.timeSpentTextView.text = ""

                            var totalTime = 0L
                            var isCurrent = false
                            timeEntries.forEach {
                                if(it.endTime != -1L){
                                    totalTime += it.getDisplayedTotalTime()
                                    when(it.getWhichForced()){
                                        0 -> container.timeSpentTextView.setTextColor(resources.getColor(R.color.gray, mainActivity.theme))
                                        1, 2 -> container.timeSpentTextView.setTextColor(resources.getColor(R.color.design_default_color_secondary, mainActivity.theme))
                                        3 -> container.timeSpentTextView.setTextColor(resources.getColor(R.color.green, mainActivity.theme))
                                    }
                                } else {
                                    isCurrent = true
                                }
                            }

                            if(!isCurrent) {
                                if (totalTime != 0L) {
                                    container.timeSpentTextView.text = TimeUtils.timeToString(totalTime)
                                }
                            } else {
                                container.timeSpentTextView.text = getString(R.string.current_session)
                                container.timeSpentTextView.setTextColor(resources.getColor(R.color.pink, mainActivity.theme))
                            }

                            if(timeEntries.size > 1){
                                container.timeSpentTextView.setTextColor(resources.getColor(R.color.pink, mainActivity.theme))
                            }

                        })
                        liveDatas.add(data)

                        container.view.setOnClickListener {
                            mainActivity.showOverlay(TimeEntryCreateFragment(epoch))
                        }

                        when (day.date) {
                            today -> {
                                container.container.setBackgroundResource(R.color.a_little_bit_less_transparent)
                            }
                        }
                    } else {
                        container.dayTextView.setTextColor(Color.GRAY)
                    }
                }
            }
            cv.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    container.legendLayout.children.map { it as TextView }.forEachIndexed { index, tv ->
                        tv.text = daysOfWeek[index].getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(
                            Locale.getDefault())
                    }
                }
            }
            cv.monthScrollListener = { month ->
                mainActivity.updateMonth(month.yearMonth)

                val from = month.yearMonth.atDay(1).toEpochDay() * SECONDS_IN_ONE_DAY
                val to = (month.yearMonth.atEndOfMonth().toEpochDay() + 1) * SECONDS_IN_ONE_DAY - 1

                val data = timeEntryDao.getAllBetweenTerminated(from, to)

                data.observe(mainActivity, { timeEntries : List<TimeEntry> ->
                    var totalTime = 0L
                    timesAdapter.updateList(timeEntries)
                    timeEntries.forEach {
                        totalTime += it.getDisplayedTotalTime()
                    }

                    totalTimeTextView.text = TimeUtils.timeToString(totalTime)
                    currentSalaryTextView.text = String.format("%.2f", totalTime * currentHourlyPrice() / 3600)
                })

                liveDatas.add(data)
            }

            val firstMonth = yearMonth.minusMonths(10)
            val lastMonth = yearMonth.plusMonths(10)
            val firstDayOfWeek = daysOfWeek.first()
            cv.setup(firstMonth, lastMonth, firstDayOfWeek)
            cv.scrollToMonth(yearMonth)
        }
    }

    private fun currentHourlyPrice() : Double {
        return 12.28
    }

    override fun nextMonth(){
        calendarView?.let { cv ->
            cv.findFirstVisibleMonth()?.let {
                cv.scrollToMonth(it.yearMonth.next)
            }
        }
    }

    override fun previousMonth(){
        calendarView?.let { cv ->
            cv.findFirstVisibleMonth()?.let {
                cv.scrollToMonth(it.yearMonth.previous)
            }
        }
    }

    override fun close(){
        liveDatas.forEach {
            it.removeObservers(mainActivity)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(yearMonth_: YearMonth) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_YEAR, yearMonth_.year)
                    putInt(ARG_MONTH, yearMonth_.monthValue)
                }
            }
    }
}