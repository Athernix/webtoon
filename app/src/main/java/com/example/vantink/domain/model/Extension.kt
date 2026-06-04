package com.example.vantink.domain.model

data class Extension(
    val name: String,
    val pkgName: String,
    val baseUrl: String,
    val lang: String,
    val version: String,
    val codeUrl: String,
    val iconUrl: String,
    val apkUrl: String,
    val nsfw: Boolean,
    val sourceType: String = "madara",
    val isMetaProvider: Boolean = false,
    val isDirectory: Boolean = false,
    val installedVersion: String? = null,
    val isActive: Boolean = false
)

enum class ExtensionAction {
    Install,
    Update,
    Manage
}
