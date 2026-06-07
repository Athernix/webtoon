package com.example.vantink

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.vantink.di.ServiceLocator
import com.example.vantink.domain.repository.ExtensionRepository
import com.example.vantink.domain.repository.WebtoonRepository
import okhttp3.OkHttpClient

class VantInkApp : Application(), ImageLoaderFactory {

    val repository: WebtoonRepository
        get() = ServiceLocator.getRepository(this)

    val extensionRepository: ExtensionRepository
        get() = ServiceLocator.getExtensionRepository(this)
    
    val okHttpClient: OkHttpClient
        get() = ServiceLocator.getOkHttpClient()

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                okHttpClient.newBuilder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "Mozilla/5.0")
                            .header("Referer", "https://mangadex.org/")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.20) // Reduced to prevent OOM
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(this.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        
        // Initialize Global Preferences
        com.example.vantink.data.local.AppPreferences.init(this)
        
        // Safety handler to log and prevent silent exit
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("VantInkCrash", "Uncaught exception on thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
