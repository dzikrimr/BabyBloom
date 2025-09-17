package com.example.bubtrack.domain.activities

import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow

interface ActivitiesRepo {
    suspend fun getAllActivities(): Flow<Resource<List<Activity>>>
    suspend fun addActivity(activity: Activity): Flow<Resource<Unit>>
}