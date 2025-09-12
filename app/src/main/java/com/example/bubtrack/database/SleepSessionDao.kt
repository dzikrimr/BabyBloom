// data/database/SleepSessionDao.kt
package com.example.bubtrack.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Update

@Dao
interface SleepSessionDao {
    @Insert
    suspend fun insertSession(session: SleepSessionEntity)

    @Update
    suspend fun updateSession(session: SleepSessionEntity)
}