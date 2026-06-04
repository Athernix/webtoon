package com.example.vantink.domain.model

data class Chapter(
    val id: String,
    val webtoonId: String,
    val title: String,
    val number: Float,
    val pages: List<String> = emptyList(),
    val uploadDate: String = ""
)
