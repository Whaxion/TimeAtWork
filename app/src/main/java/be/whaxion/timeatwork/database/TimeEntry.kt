package be.whaxion.timeatwork.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_entries")
data class TimeEntry(
    @PrimaryKey(autoGenerate = true) val uid : Long = 0,
    @ColumnInfo(name = "start_time") var startTime : Long,
    @ColumnInfo(name = "forced_start_time") var forcedStartTime : Long = -1,
    @ColumnInfo(name = "end_time") var endTime : Long,
    @ColumnInfo(name = "forced_end_time") var forcedEndTime : Long = -1,
){
    fun getDisplayedStartTime() : Long {
        var time = forcedStartTime
        if(time == -1L)
            time = startTime

        return time / 60 * 60 // removal of seconds
    }
    fun getDisplayedEndTime() : Long {
        var time = forcedEndTime
        if(time == -1L)
            time = endTime

        return time / 60 * 60 // removal of seconds
    }
    fun getDisplayedTotalTime() : Long {
        return getDisplayedEndTime() - getDisplayedStartTime()
    }

    fun getWhichForced() : Int {
        var returnVal = 0
        if(forcedStartTime != -1L){
            returnVal += 1
        }
        if(forcedEndTime != -1L){
            returnVal += 2
        }

        return returnVal
    }
}