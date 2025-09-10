package com.example.bubtrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [SleepSessionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SleepDatabase : RoomDatabase() {
    abstract fun sleepDao(): SleepDao

    companion object {
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        fun getDatabase(context: Context): SleepDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SleepDatabase::class.java,
                    "sleep_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}