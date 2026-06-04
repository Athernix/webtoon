package com.example.vantink.domain.model

data class SearchFilter(
    val query: String = "",
    val genres: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val status: String? = null,
    val format: String? = null,
    val sort: String = "TRENDING_DESC",
    val page: Int = 1,
    val perPage: Int = 20
)
