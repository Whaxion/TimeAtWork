package be.whaxion.timeatwork.fragment

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import be.whaxion.timeatwork.MainActivity
import be.whaxion.timeatwork.R
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.database.TimeEntry
import be.whaxion.timeatwork.utils.TimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

class CurrentSessionFragment(val timeEntry : TimeEntry) : Fragment() {
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = requireActivity() as MainActivity
        return inflater.inflate(R.layout.fragment_current_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val startTime = LocalDateTime.ofEpochSecond(timeEntry.startTime, 0, ZoneOffset.UTC)
        view.findViewById<TextView>(R.id.current_session_start_time).text = TimeUtils.timeFormatter.format(startTime)

        view.findViewById<Button>(R.id.current_session_stop_session).setOnClickListener {
            timeEntry.endTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)

            mainActivity.hideOverlay()

            GlobalScope.launch(Dispatchers.IO) {
                AppDatabase.getDatabase(requireContext()).timeEntryDao().update(timeEntry)

                mainActivity.runOnUiThread {
                    val notificationManager: NotificationManager = mainActivity.getSystemService(
                        Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(timeEntry.uid.toInt())

                    Toast.makeText(mainActivity, getText(R.string.session_ended), Toast.LENGTH_SHORT).show()
                }
            }
        }

        view.findViewById<Button>(R.id.current_session_cancel).setOnClickListener {
            mainActivity.hideOverlay()
        }
    }
}