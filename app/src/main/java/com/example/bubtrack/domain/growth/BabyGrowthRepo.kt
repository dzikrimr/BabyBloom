package com.example.bubtrack.domain.growth

import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow

interface BabyGrowthRepo {

    suspend fun getStats() : Flow<Resource<GrowthStats>>
    suspend fun updateStats(growthStats: GrowthStats) : Flow<Resource<Boolean>>
    suspend fun getBabyGrowth() : Flow<Resource<List<BabyGrowth>>>
}