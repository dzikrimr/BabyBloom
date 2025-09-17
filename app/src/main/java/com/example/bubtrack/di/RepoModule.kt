package com.example.bubtrack.di

import com.example.bubtrack.data.article.ArticleRepoImpl
import com.example.bubtrack.data.auth.AuthRepoImpl
import com.example.bubtrack.data.growth.BabyGrowthRepoImpl
import com.example.bubtrack.domain.activities.ActivitiesRepo
import com.example.bubtrack.domain.article.ArticleRepo
import com.example.bubtrack.domain.auth.AuthRepo
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

    @Binds
    @Singleton
    abstract fun bindArticleRepo(
        articleRepoImpl: ArticleRepoImpl
    ) : ArticleRepo

    @Binds
    @Singleton
    abstract fun bindAuthRepo(
        authRepoImpl: AuthRepoImpl
    ): AuthRepo

}