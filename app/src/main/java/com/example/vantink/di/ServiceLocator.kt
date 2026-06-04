package com.example.vantink.di

import android.content.Context
import com.example.vantink.data.local.AppDatabase
import com.example.vantink.data.remote.MangaDexApiService
import com.example.vantink.data.remote.metadata.AniListApiService
import com.example.vantink.data.repository.ExtensionRepositoryImpl
import com.example.vantink.data.repository.WebtoonRepositoryImpl
import com.example.vantink.data.scraper.AniListMangaDexSource
import com.example.vantink.data.scraper.SourceFactory
import com.example.vantink.domain.repository.ExtensionRepository
import com.example.vantink.domain.repository.WebtoonRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Service Locator (Java-friendly Design Pattern)
 * Centralizes dependency management to prevent crashes due to inconsistent initialization.
 */
object ServiceLocator {

    @Volatile
    private var database: AppDatabase? = null
    
    @Volatile
    private var repository: WebtoonRepository? = null

    @Volatile
    private var extensionRepository: ExtensionRepository? = null

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE // Reduce logs for performance
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun getDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getInstance(context)
            database = instance
            instance
        }
    }

    fun getRepository(context: Context): WebtoonRepository {
        return repository ?: synchronized(this) {
            val db = getDatabase(context)
            
            val retrofit = Retrofit.Builder()
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())

            val aniListApi = retrofit.baseUrl(AniListApiService.BASE_URL).build().create(AniListApiService::class.java)
            val mangaDexApi = retrofit.baseUrl(MangaDexApiService.BASE_URL).build().create(MangaDexApiService::class.java)

            val primarySource = AniListMangaDexSource(aniListApi, mangaDexApi)
            val sourceFactory = SourceFactory(aniListApi, mangaDexApi)

            val instance = WebtoonRepositoryImpl(
                context.applicationContext,
                primarySource,
                sourceFactory,
                db.webtoonDao,
                db.downloadDao,
                db.sourceDao,
                db.repositoryDao,
                db.activeExtensionDao
            )
            repository = instance
            instance
        }
    }

    fun getExtensionRepository(context: Context): ExtensionRepository {
        return extensionRepository ?: synchronized(this) {
            val db = getDatabase(context)
            val retrofit = Retrofit.Builder()
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
            val aniListApi = retrofit.baseUrl(AniListApiService.BASE_URL).build().create(AniListApiService::class.java)
            val mangaDexApi = retrofit.baseUrl(MangaDexApiService.BASE_URL).build().create(MangaDexApiService::class.java)
            val sourceFactory = SourceFactory(aniListApi, mangaDexApi)

            val instance = ExtensionRepositoryImpl(client, sourceFactory, db.activeExtensionDao)
            extensionRepository = instance
            instance
        }
    }

    fun getOkHttpClient(): OkHttpClient = client
}
