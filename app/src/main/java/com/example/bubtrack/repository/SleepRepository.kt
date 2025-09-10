package com.example.bubtrack.repository

import com.example.bubtrack.data.database.SleepDao
import com.example.bubtrack.data.database.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val sleepDao: SleepDao) {

    fun getAllSessions(): Flow<List<SleepSessionEntity>> {
        return sleepDao.getAllSessions()
    }

    suspend fun insertSession(session: SleepSessionEntity): Long {
        return sleepDao.insertSession(session)
    }

    suspend fun updateSession(session: SleepSessionEntity) {
        sleepDao.updateSession(session)
    }

    suspend fun getSessionById(id: Long): SleepSessionEntity? {
        return sleepDao.getSessionById(id)
    }

    suspend fun deleteSession(session: SleepSessionEntity) {
        sleepDao.deleteSession(session)
    }
}