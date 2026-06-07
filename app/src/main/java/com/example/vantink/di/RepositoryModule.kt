package com.example.vantink.di

import com.example.vantink.data.repository.ExtensionRepositoryImpl
import com.example.vantink.data.repository.WebtoonRepositoryImpl
import com.example.vantink.domain.repository.ExtensionRepository
import com.example.vantink.domain.repository.WebtoonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWebtoonRepository(
        webtoonRepositoryImpl: WebtoonRepositoryImpl
    ): WebtoonRepository

    @Binds
    @Singleton
    abstract fun bindExtensionRepository(
        extensionRepositoryImpl: ExtensionRepositoryImpl
    ): ExtensionRepository
}
