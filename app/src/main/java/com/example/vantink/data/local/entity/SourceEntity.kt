package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val type: String, // "madara", "anilist_md", etc.
    val iconUrl: String = "",
    val isEnabled: Boolean = true,
    val lang: String = "en"
)
