package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ActiveExtensions")
data class ActiveExtensionEntity(
    @PrimaryKey val pkgName: String,
    val name: String,
    val baseUrl: String,
    val lang: String,
    val version: String,
    val iconUrl: String = "",
    val installedAt: Long = System.currentTimeMillis()
)
