package com.example.artgallery.model

data class ArtworkFilter(
    val query: String = "",
    val selectedCategories: List<String> = emptyList()
)
