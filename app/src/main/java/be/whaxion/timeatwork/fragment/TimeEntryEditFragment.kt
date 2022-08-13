package be.whaxion.timeatwork.fragment

import android.app.TimePickerDialog
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class TimeEntryEditFragment(private val timeEntry : TimeEntry) : Fragment(), TimePickerDialog.OnTimeSetListener {
    private lateinit var startTime : TextView
    private lateinit var forcedStartTime : TextView
    private lateinit var endTime : TextView
    private lateinit var forcedEndTime : TextView

    private lateinit var mainActivity: MainActivity

    private var currentlyEditing = 0 // 0 = none, 1 = startTime, 2 = end

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = requireActivity() as MainActivity
        return inflater.inflate(R.layout.time_entry_edit_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startTime = view.findViewById(R.id.edit_time_entry_start_time)
        forcedStartTime = view.findViewById(R.id.edit_time_entry_forced_start_time)
        endTime = view.findViewById(R.id.edit_time_entry_end_time)
        forcedEndTime = view.findViewById(R.id.edit_time_entry_forced_end_time)

        startTime.text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC))
        if(timeEntry.forcedStartTime != -1L){
            startTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            forcedStartTime.apply {
                text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.forcedStartTime, 0, ZoneOffset.UTC))
                visibility = View.VISIBLE
            }
        }

        endTime.text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.endTime, 0, ZoneOffset.UTC))
        if(timeEntry.forcedEndTime != -1L){
            endTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            forcedEndTime.apply {
                text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.forcedEndTime, 0, ZoneOffset.UTC))
                visibility = View.VISIBLE
            }
        }

        view.findViewById<Button>(R.id.edit_time_entry_start_time_button).setOnClickListener {
            currentlyEditing = 1
            openTimePicker(timeEntry.getDisplayedStartTime())
        }

        view.findViewById<Button>(R.id.edit_time_entry_end_time_button).setOnClickListener {
            currentlyEditing = 2
            openTimePicker(timeEntry.getDisplayedEndTime())
        }

        view.findViewById<Button>(R.id.edit_time_entry_save_button).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).timeEntryDao().update(timeEntry)
                mainActivity.hideOverlay()
            }
        }

        view.findViewById<Button>(R.id.edit_time_entry_cancel_button).setOnClickListener {
            mainActivity.hideOverlay()
        }

        view.findViewById<Button>(R.id.edit_time_entry_delete_button).setOnClickListener {
            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).timeEntryDao().delete(timeEntry)
                mainActivity.hideOverlay()
            }
        }
    }

    private fun openTimePicker(time : Long){
        val localDateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
        TimePickerDialog(mainActivity, this, localDateTime.hour, localDateTime.minute, true).show()
    }

    private fun correctEndTime(){
        // If less than startTime => add one day
        if(timeEntry.forcedEndTime < timeEntry.getDisplayedStartTime()){
            timeEntry.forcedEndTime += SECONDS_IN_ONE_DAY
        }
        // If more than 1 day => remove one day
        if(timeEntry.forcedEndTime > timeEntry.getDisplayedStartTime() + SECONDS_IN_ONE_DAY){
            timeEntry.forcedEndTime -= SECONDS_IN_ONE_DAY
        }
    }

    override fun onTimeSet(view: TimePicker?, hour: Int, minute: Int) {
        when(currentlyEditing){
            1 -> {
                val oldStartTime = LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC)
                val newStartTime = LocalDateTime.of(oldStartTime.year, oldStartTime.month, oldStartTime.dayOfMonth, hour, minute)
                timeEntry.forcedStartTime = newStartTime.toEpochSecond(ZoneOffset.UTC)
                startTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                forcedStartTime.apply {
                    text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.forcedStartTime, 0, ZoneOffset.UTC))
                    visibility = View.VISIBLE
                }
            }
            2 -> {
                val oldEndTime = LocalDateTime.ofEpochSecond(timeEntry.endTime, 0, ZoneOffset.UTC)
                val newEndTime = LocalDateTime.of(oldEndTime.year, oldEndTime.month, oldEndTime.dayOfMonth, hour, minute)
                timeEntry.forcedEndTime = newEndTime.toEpochSecond(ZoneOffset.UTC)
                endTime.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                forcedEndTime.apply {
                    text = TimeUtils.timeFormatter.format(LocalDateTime.ofEpochSecond(timeEntry.forcedEndTime, 0, ZoneOffset.UTC))
                    visibility = View.VISIBLE
                }
            }
        }

        correctEndTime()
        currentlyEditing = 0
    }

}