package be.whaxion.timeatwork

import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import be.whaxion.timeatwork.database.AppDatabase
import be.whaxion.timeatwork.fragment.*
import be.whaxion.timeatwork.utils.TimeUtils
import com.kizitonwose.calendarview.utils.next
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.YearMonth

class MainActivity : AppCompatActivity() {
    private var overlay : LinearLayout? = null

    private lateinit var currentFragment : HasMonth
    private var currentYearMonth = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateMonth()

        currentFragment = CalendarFragment.newInstance(currentYearMonth)
        supportFragmentManager.beginTransaction().add(R.id.main_fragment, currentFragment).commit()

        overlay = findViewById(R.id.overlay_container)
        overlay?.setOnClickListener {
            hideOverlay()
        }

        val calendarIV = findViewById<ImageView>(R.id.footer_calendar_button_iv)
        val calendarTV = findViewById<TextView>(R.id.footer_calendar_button_tv)
        val reportsIV = findViewById<ImageView>(R.id.footer_reports_button_iv)
        val reportsTV = findViewById<TextView>(R.id.footer_reports_button_tv)

        val tintSelected = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_on_primary))
        val tintDeselected = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.color_on_primary_deselected))

        findViewById<LinearLayout>(R.id.footer_calendar_button).setOnClickListener {
            if(currentFragment !is CalendarFragment){
                currentFragment.close()
                currentFragment = CalendarFragment.newInstance(currentYearMonth)
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment, currentFragment).commit()

                calendarTV.visibility = View.VISIBLE
                calendarIV.imageTintList = tintSelected

                reportsTV.visibility = View.GONE
                reportsIV.imageTintList = tintDeselected
            }
        }
        findViewById<LinearLayout>(R.id.footer_reports_button).setOnClickListener {
            if(currentFragment !is ReportsFragment){
                currentFragment.close()
                currentFragment = ReportsFragment.newInstance(currentYearMonth)
                supportFragmentManager.beginTransaction().replace(R.id.main_fragment, currentFragment).commit()

                reportsTV.visibility = View.VISIBLE
                reportsIV.imageTintList = tintSelected

                calendarTV.visibility = View.GONE
                calendarIV.imageTintList = tintDeselected
            }
        }

        findViewById<LinearLayout>(R.id.footer_add_button).setOnClickListener {
            val fragment = AddMenuFragment()
            showOverlay(fragment)
        }

        findViewById<ImageView>(R.id.next_month_image).setOnClickListener {
            currentFragment.nextMonth()
        }

        findViewById<ImageView>(R.id.previous_month_image).setOnClickListener {
            currentFragment.previousMonth()
        }


        GlobalScope.launch(Dispatchers.IO) {
            val data = AppDatabase.getDatabase(this@MainActivity).timeEntryDao().findUnterminated()
            if (data.isNotEmpty()) {
                data.forEach {
                    AddMenuFragment.createNotification(it.uid.toInt(), this@MainActivity)
                    val currentSessionFragment = CurrentSessionFragment(it)
                    showOverlay(currentSessionFragment)
                }
            }
        }
    }

    fun updateMonth(yearMonth: YearMonth = currentYearMonth){
        currentYearMonth = yearMonth
        val title = "${TimeUtils.monthTitleFormatter.format(currentYearMonth)} ${currentYearMonth.year}"
        findViewById<TextView>(R.id.month_year_text).text = title
    }

    fun showOverlay(fragment : Fragment? = null){
        runOnUiThread {
            if(fragment != null)
                supportFragmentManager.beginTransaction().replace(R.id.overlay_fragment, fragment).commit()

            overlay?.visibility = View.VISIBLE
        }
    }

    fun hideOverlay(){
        runOnUiThread {
            overlay?.visibility = View.GONE
        }
    }

}