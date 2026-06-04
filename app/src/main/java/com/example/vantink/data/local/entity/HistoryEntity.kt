package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val webtoonId: String,
    val title: String,
    val thumbnailUrl: String,
    val chapterId: String,
    val chapterTitle: String,
    val chapterNumber: Float,
    val scrollPosition: Int = 0,
    val lastReadDate: Long = System.currentTimeMillis()
)
