package com.example.bubtrack.data.growth

import com.example.bubtrack.domain.growth.BabyGrowthModel
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import com.example.bubtrack.domain.growth.GrowthStats
import com.example.bubtrack.utill.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject

class BabyGrowthRepoImpl @Inject constructor() : BabyGrowthRepo {
    override suspend fun getStats(): Flow<Resource<GrowthStats>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateStats(growthStats: GrowthStats): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override suspend fun getBabyGrowth(): Flow<Resource<List<BabyGrowthModel>>> {
        val calendar = Calendar.getInstance()
        return flow {
            emit(Resource.Loading())

            // Scenario 1: Empty data (comment/uncomment to test different scenarios)
            // return emptyList()

            // Scenario 2: Single data point
            // return listOf(
            //     BabyGrowthModel(
            //         id = "1",
            //         date = calendar.time,
            //         weight = 3.2f,
            //         height = 52f,
            //         headCircumference = 35f,
            //         armLength = 18f,
            //         ageInMonths = 0
            //     )
            // )
            val data =  listOf(
                BabyGrowthModel(
                    id = "1",
                    date = System.currentTimeMillis(),
                    weight = 3.2,
                    height = 52.0,
                    headCircumference = 35.0,
                    armLength = 18.0,
                    ageInMonths = 0
                ),
                BabyGrowthModel(
                    id = "2",
                    date = System.currentTimeMillis(),
                    weight = 4.1,
                    height = 55.0,
                    headCircumference = 36.0,
                    armLength = 20.0,
                    ageInMonths = 1
                ),
                BabyGrowthModel(
                    id = "3",
                    date = System.currentTimeMillis(),
                    weight = 5.3,
                    height = null, // Some missing data
                    headCircumference = 37.0,
                    armLength = null,
                    ageInMonths = 2
                )
            )
            emit(Resource.Success(data))
        }
    }
}