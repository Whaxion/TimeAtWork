package be.whaxion.timeatwork

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import be.whaxion.timeatwork.database.TimeEntry
import be.whaxion.timeatwork.fragment.TimeEntryEditFragment
import be.whaxion.timeatwork.utils.TimeUtils
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.collections.ArrayList

class TimeAdapter(private val mainActivity: MainActivity) : RecyclerView.Adapter<TimeAdapter.TimeViewHolder>() {
    private val data : ArrayList<TimeEntry> = arrayListOf()

    fun updateList(newData : List<TimeEntry>){
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.time_item, parent, false)
        return TimeViewHolder(listItem)
    }

    override fun onBindViewHolder(holder: TimeViewHolder, position: Int) {
        val startTime = LocalDateTime.ofEpochSecond(data[position].getDisplayedStartTime(), 0, ZoneOffset.UTC)
        val endTime = LocalDateTime.ofEpochSecond(data[position].getDisplayedEndTime(), 0, ZoneOffset.UTC)
        val totalTime = data[position].getDisplayedTotalTime()
        val whichForced = data[position].getWhichForced()

        holder.dateTextView.text = TimeUtils.dateFormatter.format(startTime)
        holder.startTimeTextView.text = TimeUtils.timeFormatter.format(startTime)
        holder.endTimeTextView.text = TimeUtils.timeFormatter.format(endTime)
        holder.totalTimeTextView.text = TimeUtils.timeToString(totalTime)

        when(whichForced){
            1, 2 -> holder.totalTimeTextView.setTextColor(mainActivity.resources.getColor(R.color.design_default_color_secondary, mainActivity.theme))
            3 -> holder.totalTimeTextView.setTextColor(mainActivity.resources.getColor(R.color.green, mainActivity.theme))
        }

        if(whichForced and 1 == 1){
            holder.startTimeTextView.setTextColor(mainActivity.resources.getColor(R.color.green, mainActivity.theme))
        }
        if(whichForced and 2 == 2){
            holder.endTimeTextView.setTextColor(mainActivity.resources.getColor(R.color.green, mainActivity.theme))
        }

        holder.container.setOnClickListener {
            val fragment = TimeEntryEditFragment(data[position])
            mainActivity.showOverlay(fragment)
        }
    }

    override fun getItemCount(): Int = data.size

    class TimeViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val container : ConstraintLayout = itemView.findViewById(R.id.time_item_container)
        val dateTextView : TextView = itemView.findViewById(R.id.time_item_date)
        val startTimeTextView : TextView = itemView.findViewById(R.id.time_item_start_time)
        val endTimeTextView : TextView = itemView.findViewById(R.id.time_item_end_time)
        val totalTimeTextView : TextView = itemView.findViewById(R.id.time_item_total_time)
    }
}