package com.example.vantink.di

import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitBuilder(client: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
    }

    @Provides
    @Singleton
    fun provideAniListApiService(builder: Retrofit.Builder): AniListApiService {
        return builder.baseUrl(AniListApiService.BASE_URL).build().create(AniListApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideMangaDexApiService(builder: Retrofit.Builder): MangaDexApiService {
        return builder.baseUrl(MangaDexApiService.BASE_URL).build().create(MangaDexApiService::class.java)
    }
}
