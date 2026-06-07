package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val chapterId: String,
    val webtoonId: String,
    val webtoonTitle: String,
    val chapterTitle: String,
    val chapterNumber: Float,
    val status: String, // PENDING, DOWNLOADING, COMPLETED, ERROR
    val progress: Int = 0,
    val localPath: String = "",
    val errorMessage: String? = null,
    val createdDate: Long = System.currentTimeMillis()
)
