package be.whaxion.timeatwork.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import be.whaxion.timeatwork.MainActivity
import be.whaxion.timeatwork.R
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.database.TimeEntry
import be.whaxion.timeatwork.utils.SECONDS_IN_ONE_DAY
import be.whaxion.timeatwork.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TimeEntryCreateFragment(startTime : Long = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)) : Fragment(), TimePickerDialog.OnTimeSetListener,
    DatePickerDialog.OnDateSetListener {
    private lateinit var mainActivity: MainActivity

    private var currentlyEditing = 0 // 0 = none, 1 = startTime, 2 = end, 3 = date
    private val timeEntry = TimeEntry(startTime = startTime, forcedStartTime = startTime, endTime = startTime, forcedEndTime = startTime)
    private lateinit var startTimeTV : TextView
    private lateinit var endTimeTV : TextView
    private lateinit var dateTV : TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = requireActivity() as MainActivity
        return inflater.inflate(R.layout.fragment_time_entry_create, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTimeTV = view.findViewById(R.id.create_time_entry_start_time)
        endTimeTV = view.findViewById(R.id.create_time_entry_end_time)
        dateTV = view.findViewById(R.id.create_time_entry_date)

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        dateTV.text = dateFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC))

        view.findViewById<Button>(R.id.create_time_entry_start_time_button).setOnClickListener {
            currentlyEditing = 1
            openTimePicker(timeEntry.startTime)
        }

        view.findViewById<Button>(R.id.create_time_entry_end_time_button).setOnClickListener {
            currentlyEditing = 2
            openTimePicker(timeEntry.endTime)
        }

        view.findViewById<Button>(R.id.create_time_entry_date_button).setOnClickListener {
            currentlyEditing = 3
            openDatePicker(timeEntry.startTime)
        }

        view.findViewById<Button>(R.id.create_time_entry_save_button).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).timeEntryDao().insert(timeEntry)
                mainActivity.hideOverlay()
            }
        }

        view.findViewById<Button>(R.id.create_time_entry_cancel_button).setOnClickListener {
            mainActivity.hideOverlay()
        }
    }

    private fun openTimePicker(time : Long){
        val localDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
        TimePickerDialog(mainActivity, this, localDateTime.hour, localDateTime.minute, true).show()
    }

    private fun openDatePicker(time : Long){
        val localDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
        DatePickerDialog(mainActivity, this, localDateTime.year, localDateTime.monthValue - 1, localDateTime.dayOfMonth).show()
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        when(currentlyEditing){
            1 -> {
                val oldStartTime = LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC)
                val newStartTime = LocalDateTime.of(oldStartTime.year, oldStartTime.month, oldStartTime.dayOfMonth, hour, minute)
                timeEntry.startTime = newStartTime.toEpochSecond(ZoneOffset.UTC)
                timeEntry.forcedStartTime = timeEntry.startTime

                startTimeTV.text = TimeUtils.timeFormatter.format(newStartTime)
            }
            2 -> {
                val oldEndTime = LocalDateTime.ofEpochSecond(timeEntry.endTime, 0, ZoneOffset.UTC)
                val newEndTime = LocalDateTime.of(oldEndTime.year, oldEndTime.month, oldEndTime.dayOfMonth, hour, minute)
                timeEntry.endTime = newEndTime.toEpochSecond(ZoneOffset.UTC)

                endTimeTV.text = TimeUtils.timeFormatter.format(newEndTime)
            }
        }

        correctEndTime()
        currentlyEditing = 0
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, day: Int) {
        when(currentlyEditing){
            3 -> {
                val oldStartTime = LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC)
                val newStartTime = LocalDateTime.of(year, month + 1, day, oldStartTime.hour, oldStartTime.minute)
                timeEntry.startTime = newStartTime.toEpochSecond(ZoneOffset.UTC)
                timeEntry.forcedStartTime = timeEntry.startTime

                val oldEndTime = LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC)
                val newEndTime = LocalDateTime.of(year, month + 1, day, oldEndTime.hour, oldEndTime.minute)
                timeEntry.endTime = newEndTime.toEpochSecond(ZoneOffset.UTC)
                timeEntry.forcedEndTime = timeEntry.endTime

                val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
                dateTV.text = dateFormatter.format(newStartTime)
            }
        }

        correctEndTime()
        currentlyEditing = 0
    }

    private fun correctEndTime(){
        // If less than startTime => add one day
        if(timeEntry.endTime < timeEntry.startTime){
            timeEntry.endTime += SECONDS_IN_ONE_DAY
        }
        // If more than 1 day => remove one day
        if(timeEntry.endTime > timeEntry.startTime + SECONDS_IN_ONE_DAY){
            timeEntry.endTime -= SECONDS_IN_ONE_DAY
        }

        timeEntry.forcedEndTime = timeEntry.endTime
    }
}