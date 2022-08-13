package be.whaxion.timeatwork.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TimeEntryDao {
    @Query("SELECT * FROM time_entries ORDER BY start_time")
    fun getAll() : LiveData<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE start_time >= :start AND start_time <= :end ORDER BY start_time")
    fun getAllBetween(start : Long, end : Long) : LiveData<List<TimeEntry>>

    @Query("SELECT * FROM time_entries WHERE start_time >= :start AND start_time <= :end AND end_time != -1 ORDER BY start_time")
    fun getAllBetweenTerminated(start : Long, end : Long) : LiveData<List<TimeEntry>>


    @Query("SELECT * FROM time_entries WHERE end_time == -1 ORDER BY start_time")
    suspend fun findUnterminated() : List<TimeEntry>

    @Insert
    suspend fun insert(timeEntry : TimeEntry) : Long

    @Update
    suspend fun update(timeEntry: TimeEntry)

    @Delete
    suspend fun delete(timeEntry : TimeEntry)
}