package com.example.bubtrack.domain.home

import com.example.bubtrack.domain.activities.Activity
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.domain.profile.BabyProfile

interface HomeRepo {
    suspend fun getBabyProfile(userId: String): Result<BabyProfile>
    suspend fun getLatestGrowthStats(userId: String): Result<GrowthStats>
    suspend fun getUpcomingActivities(userId: String): Result<List<Activity>>
    fun observeGrowthStats(userId: String): kotlinx.coroutines.flow.Flow<Result<GrowthStats>>
    fun observeUpcomingActivities(userId: String): kotlinx.coroutines.flow.Flow<Result<List<Activity>>>
    fun calculateAge(birthDateMillis: Long): String
}