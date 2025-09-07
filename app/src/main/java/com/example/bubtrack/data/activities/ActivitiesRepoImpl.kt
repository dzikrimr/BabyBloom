package com.example.bubtrack.data.activities

import com.example.bubtrack.domain.activities.ActivitiesRepo
import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.activities.dummyActivities
import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ActivitiesRepoImpl @Inject constructor() : ActivitiesRepo {
    override suspend fun getAllActivities(): Flow<Resource<List<Activity>>> {
        return flow {
            emit(Resource.Loading())
            try {
                emit(Resource.Success(dummyActivities))
            } catch (e: Exception){
                emit(Resource.Error(e.message.toString()))
            }
        }
    }
}