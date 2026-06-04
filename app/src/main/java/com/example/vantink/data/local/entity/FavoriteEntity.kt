package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val id: String, // webtoonId
    val title: String,
    val thumbnailUrl: String,
    val addedDate: Long = System.currentTimeMillis()
)
