package com.example.bubtrack.di

import com.example.bubtrack.data.growth.BabyGrowthRepoImpl
import com.example.bubtrack.domain.growth.BabyGrowthRepo
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepoModule {

    @Binds
    @Singleton
    abstract fun bindGrowthRepo(
        growthRepoImpl: BabyGrowthRepoImpl
    ) : BabyGrowthRepo
}