package com.example.bubtrack.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_sessions ORDER BY startTime DESC")
    fun getAllSessions(): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SleepSessionEntity?

    @Insert
    suspend fun insertSession(session: SleepSessionEntity): Long

    @Update
    suspend fun updateSession(session: SleepSessionEntity)

    @Delete
    suspend fun deleteSession(session: SleepSessionEntity)
}