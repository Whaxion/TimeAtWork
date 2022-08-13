package be.whaxion.timeatwork.fragment

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import be.whaxion.timeatwork.HasMonth
import be.whaxion.timeatwork.MainActivity
import be.whaxion.timeatwork.R
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.database.TimeEntry
import be.whaxion.timeatwork.database.TimeEntryDao
import be.whaxion.timeatwork.utils.SECONDS_IN_ONE_DAY
import be.whaxion.timeatwork.utils.TimeUtils
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.ArrayList

private const val ARG_YEAR = "ARG_YEAR"
private const val ARG_MONTH = "ARG_MONTH"

class ReportsFragment : HasMonth() {
    private var yearMonth: YearMonth = YearMonth.now()
    private val liveDatas : ArrayList<LiveData<List<TimeEntry>>> = arrayListOf()

    private lateinit var mainActivity : MainActivity
    private lateinit var timeEntryDao : TimeEntryDao
    private lateinit var messageTV : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            yearMonth = YearMonth.of(it.getInt(ARG_YEAR), it.getInt(ARG_MONTH))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = requireActivity() as MainActivity

        timeEntryDao = AppDatabase.getDatabase(mainActivity).timeEntryDao()
        messageTV = view.findViewById(R.id.reports_message)
        monthUpdated()

        view.findViewById<Button>(R.id.reports_copy_button).setOnClickListener {
            val clipboardManager = mainActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Report of the month", messageTV.text))

            Toast.makeText(mainActivity, getText(R.string.copy_success), Toast.LENGTH_SHORT).show()
        }
    }

    fun monthUpdated(){
        val from = yearMonth.atDay(1).toEpochDay() * SECONDS_IN_ONE_DAY
        val to = (yearMonth.atEndOfMonth().toEpochDay() + 1) * SECONDS_IN_ONE_DAY - 1

        val data = timeEntryDao.getAllBetween(from, to)

        data.observe(mainActivity, { timeEntries : List<TimeEntry> ->
            var text = ""
            var totalTime = 0L
            timeEntries.forEach {
                val startTime = LocalDateTime.ofEpochSecond(it.getDisplayedStartTime(), 0, ZoneOffset.UTC)
                val endTime = LocalDateTime.ofEpochSecond(it.getDisplayedEndTime(), 0, ZoneOffset.UTC)
                val totalTimeOfDay = it.getDisplayedTotalTime()

                text += "${TimeUtils.dayMonthFormatter.format(startTime)}: ${TimeUtils.timeFormatter.format(startTime)}->${TimeUtils.timeFormatter.format(endTime)} ${TimeUtils.timeToString(totalTimeOfDay)}\n"
                totalTime += totalTimeOfDay
            }

            text += "\nTotal: ${TimeUtils.timeToString(totalTime)}"
            messageTV.text = text
        })

        liveDatas.add(data)
    }

    override fun nextMonth(){
        yearMonth = yearMonth.next
        mainActivity.updateMonth(yearMonth)
        monthUpdated()
    }

    override fun previousMonth(){
        yearMonth = yearMonth.previous
        mainActivity.updateMonth(yearMonth)
        monthUpdated()
    }

    override fun close(){
        liveDatas.forEach {
            it.removeObservers(mainActivity)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(yearMonth_: YearMonth) =
            ReportsFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_YEAR, yearMonth_.year)
                    putInt(ARG_MONTH, yearMonth_.monthValue)
                }
            }
    }
}