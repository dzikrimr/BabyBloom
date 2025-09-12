package com.example.bubtrack.domain.ai

import com.example.bubtrack.database.SleepSessionEntity

interface SleepRepository {
    suspend fun insertSession(session: SleepSessionEntity): Long
    suspend fun updateSession(session: SleepSessionEntity)
    suspend fun getAllSessions(): List<SleepSessionEntity>
    suspend fun getSessionById(id: Long): SleepSessionEntity?
    suspend fun deleteSession(session: SleepSessionEntity)
}

// Simple in-memory implementation for testing
class SimpleSleepRepository : SleepRepository {
    private val sessions = mutableMapOf<Long, SleepSessionEntity>()

    override suspend fun insertSession(session: SleepSessionEntity): Long {
        sessions[session.id] = session
        return session.id
    }

    override suspend fun updateSession(session: SleepSessionEntity) {
        sessions[session.id] = session
    }

    override suspend fun getAllSessions(): List<SleepSessionEntity> {
        return sessions.values.toList()
    }

    override suspend fun getSessionById(id: Long): SleepSessionEntity? {
        return sessions[id]
    }

    override suspend fun deleteSession(session: SleepSessionEntity) {
        sessions.remove(session.id)
    }
}