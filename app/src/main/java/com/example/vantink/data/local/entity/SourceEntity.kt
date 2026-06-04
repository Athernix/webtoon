package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sources")
data class SourceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val type: String, // "madara", "mangastream", "anilist_md"
    val iconUrl: String = "",
    val isEnabled: Boolean = true,
    val lang: String = "en",
    val version: String = "1.0.0",
    val isNsfw: Boolean = false,
    val repoUrl: String = "" // To track which repo it came from
)
