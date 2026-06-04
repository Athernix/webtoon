package com.example.vantink.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extension_repositories")
data class RepositoryEntity(
    @PrimaryKey val url: String,
    val name: String
)
