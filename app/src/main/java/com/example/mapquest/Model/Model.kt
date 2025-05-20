package com.example.mapquest.Model

data class LocationResult(
    val displayName: String,
    val address: String,
    val lat: Double,
    val lon: Double
)

data class SearchState(
    val isLoading: Boolean = false,
    val results: List<LocationResult> = emptyList(),
    val errorMessage: String? = null
)