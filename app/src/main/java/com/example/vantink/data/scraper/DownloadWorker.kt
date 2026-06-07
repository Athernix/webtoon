package com.example.vantink.data.scraper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vantink.VantInkApp
import com.example.vantink.data.local.AppDatabase
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val chapterId = inputData.getString("chapterId") ?: return Result.failure()
        val webtoonId = inputData.getString("webtoonId") ?: return Result.failure()
        val sourceReferer = inputData.getString("sourceReferer") ?: "" // Dynamic referer based on source
        
        val db = AppDatabase.getInstance(applicationContext)
        val app = applicationContext as VantInkApp
        val repository = app.repository
        val client = app.okHttpClient
        
        val download = db.downloadDao.getDownload(chapterId) ?: return Result.failure()
        db.downloadDao.updateDownload(download.copy(status = "DOWNLOADING", progress = 0))

        return try {
            val pagesResult = repository.getChapterDetails(chapterId)
            val pages = pagesResult.getOrNull()?.pages ?: return Result.failure()
            
            val baseDir = File(
                applicationContext.filesDir,
                "downloads/${webtoonId.toSafePathSegment()}/${chapterId.toSafePathSegment()}"
            )
            if (!baseDir.exists()) baseDir.mkdirs()
            
            var successCount = 0
            val errors = mutableListOf<String>()
            
            pages.forEachIndexed { index, urlString ->
                try {
                    if (!urlString.isValidUrl()) {
                        errors.add("Invalid URL at page ${index}: $urlString")
                        return@forEachIndexed
                    }
                    
                    val file = File(baseDir, "page_${index}.jpg")
                    
                    // Determine referer dynamically from URL or use provided one
                    val referer = if (sourceReferer.isNotEmpty()) {
                        sourceReferer
                    } else {
                        try {
                            val url = URL(urlString)
                            "${url.protocol}://${url.host}/"
                        } catch (e: Exception) {
                            "https://mangadex.org/" // Fallback
                        }
                    }
                    
                    val request = Request.Builder()
                        .url(urlString)
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                        .header("Referer", referer)
                        .header("Accept", "image/webp,image/apng,image/*,*/*;q=0.8")
                        .build()
                    
                    client.newCall(request).execute().use { response ->
                        when {
                            response.isSuccessful -> {
                                val body = response.body ?: throw Exception("Empty body")
                                body.byteStream().use { input ->
                                    FileOutputStream(file).use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                successCount++
                            }
                            response.code == 403 || response.code == 401 -> {
                                errors.add("Forbidden/Unauthorized at page ${index}: ${response.code}")
                            }
                            else -> {
                                errors.add("Failed to download page ${index}: ${response.code}")
                            }
                        }
                    }
                    
                    val progress = ((index + 1).toFloat() / pages.size * 100).toInt()
                    db.downloadDao.updateDownload(download.copy(progress = progress))
                } catch (e: Exception) {
                    errors.add("Exception downloading page ${index}: ${e.message}")
                }
            }

            if (successCount > 0) {
                db.downloadDao.updateDownload(
                    download.copy(
                        status = "COMPLETED",
                        progress = 100,
                        localPath = baseDir.absolutePath
                    )
                )
                Result.success()
            } else {
                db.downloadDao.updateDownload(
                    download.copy(
                        status = "ERROR",
                        errorMessage = errors.joinToString("; ").take(500)
                    )
                )
                Result.failure()
            }
        } catch (e: Exception) {
            db.downloadDao.updateDownload(
                download.copy(
                    status = "ERROR",
                    errorMessage = e.message?.take(500) ?: "Unknown error"
                )
            )
            Result.failure()
        }
    }

    private fun String.toSafePathSegment(): String {
        return replace(Regex("[^A-Za-z0-9._-]"), "_").take(120)
    }

    private fun String.isValidUrl(): Boolean {
        return try {
            URL(this)
            this.startsWith("http://") || this.startsWith("https://")
        } catch (e: Exception) {
            false
        }
    }
}
