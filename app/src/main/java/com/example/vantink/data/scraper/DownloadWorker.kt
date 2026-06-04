package com.example.vantink.data.scraper

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.vantink.VantInkApp
import com.example.vantink.data.local.AppDatabase
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val chapterId = inputData.getString("chapterId") ?: return Result.failure()
        val webtoonId = inputData.getString("webtoonId") ?: return Result.failure()
        
        val db = AppDatabase.getInstance(applicationContext)
        val app = applicationContext as VantInkApp
        val repository = app.repository
        val client = app.okHttpClient
        
        val download = db.downloadDao.getDownload(chapterId) ?: return Result.failure()
        db.downloadDao.updateDownload(download.copy(status = "DOWNLOADING", progress = 0))

        return try {
            val pagesResult = repository.getChapterDetails(chapterId)
            val pages = pagesResult.getOrNull()?.pages ?: return Result.failure()
            
            val baseDir = File(applicationContext.filesDir, "downloads/${webtoonId}/${chapterId}")
            if (!baseDir.exists()) baseDir.mkdirs()
            
            pages.forEachIndexed { index, urlString ->
                val file = File(baseDir, "page_${index}.jpg")
                
                val request = Request.Builder()
                    .url(urlString)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://mangadex.org/")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw Exception("Failed to download page: ${response.code}")
                    val body = response.body ?: throw Exception("Empty body")
                    body.byteStream().use { input ->
                        FileOutputStream(file).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                
                val progress = ((index + 1).toFloat() / pages.size * 100).toInt()
                db.downloadDao.updateDownload(download.copy(progress = progress))
            }

            db.downloadDao.updateDownload(download.copy(status = "COMPLETED", progress = 100, localPath = baseDir.absolutePath))
            Result.success()
        } catch (e: Exception) {
            db.downloadDao.updateDownload(download.copy(status = "ERROR"))
            Result.failure() // Use failure instead of retry for now to avoid loops on 403
        }
    }
}
