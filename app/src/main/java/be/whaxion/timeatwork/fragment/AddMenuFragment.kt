package be.whaxion.timeatwork.fragment

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import be.whaxion.timeatwork.MainActivity
import be.whaxion.timeatwork.R
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.database.TimeEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

const val CHANNEL_SESSION_ID = "CHANNEL_SESSION"

class AddMenuFragment : Fragment() {
    private lateinit var mainActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mainActivity = requireActivity() as MainActivity
        createNotificationChannel(mainActivity)

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.add_menu_start_session).setOnClickListener {
            mainActivity.hideOverlay()

            GlobalScope.launch(Dispatchers.IO) {
                val data = AppDatabase.getDatabase(mainActivity).timeEntryDao().findUnterminated()
                if(data.isEmpty()){


                    val timeEntry = TimeEntry(
                        startTime = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC),
                        endTime = -1L
                    )
                    val uid = AppDatabase.getDatabase(requireContext()).timeEntryDao().insert(timeEntry)
                    createNotification(uid.toInt(), mainActivity)
                } else {
                    mainActivity.runOnUiThread {
                        Toast.makeText(mainActivity, getText(R.string.session_unterminated), Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }

        view.findViewById<Button>(R.id.add_menu_encode_session).setOnClickListener {
            mainActivity.showOverlay(TimeEntryCreateFragment())
        }
    }

    companion object {
        fun createNotification(notificationId : Int, mainActivity: MainActivity) {
            createNotificationChannel(mainActivity)

            val intent = Intent(mainActivity, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(mainActivity, 0, intent, 0)

            val builder = NotificationCompat.Builder(mainActivity, CHANNEL_SESSION_ID)
                .setSmallIcon(R.drawable.ic_clock)
                .setContentTitle(mainActivity.getString(R.string.app_name))
                .setContentText(mainActivity.getString(R.string.notification_session_stop))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)

            with(NotificationManagerCompat.from(mainActivity)) {
                notify(notificationId, builder.build())
            }
        }

        private fun createNotificationChannel(mainActivity: MainActivity) {
            val channel = NotificationChannel(
                CHANNEL_SESSION_ID,
                "CHANNEL_SESSION",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = mainActivity.getString(R.string.notification_channel_session_description)
            }

            // Register the channel with the system
            val notificationManager: NotificationManager = mainActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}