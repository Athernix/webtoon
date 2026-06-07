package com.example.vantink.di

import android.content.Context
import com.example.vantink.data.local.AppDatabase
import com.example.vantink.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideWebtoonDao(database: AppDatabase): WebtoonDao = database.webtoonDao

    @Provides
    fun provideDownloadDao(database: AppDatabase): DownloadDao = database.downloadDao

    @Provides
    fun provideSourceDao(database: AppDatabase): SourceDao = database.sourceDao

    @Provides
    fun provideRepositoryDao(database: AppDatabase): RepositoryDao = database.repositoryDao

    @Provides
    fun provideActiveExtensionDao(database: AppDatabase): ActiveExtensionDao = database.activeExtensionDao
}
