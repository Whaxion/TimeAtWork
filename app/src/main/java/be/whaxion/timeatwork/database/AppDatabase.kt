package be.whaxion.timeatwork.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeEntry::class], exportSchema = false, version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun timeEntryDao(): TimeEntryDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room
                            .databaseBuilder(
                                context.applicationContext,
                                AppDatabase::class.java,
                                "times.db"
                            ).build()
                    }
                }
            }

            return INSTANCE!!
        }
    }
}