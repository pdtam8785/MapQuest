package com.example.mapquest.Viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mapquest.Model.LocationResult
import com.example.mapquest.Model.SearchState
import com.example.mapquest.Network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class SearchViewModel : ViewModel() {

    private val _state = MutableStateFlow(SearchState())
    val state: StateFlow<SearchState> get() = _state

    private var searchJob: Job? = null
    private val apiKey = "pk.3427dd3417dd3f5078c6ea67a3b1e3aa" //  API Key

    fun searchAddress(query: String) {
        searchJob?.cancel()

        if (query.isBlank()) {
            _state.value = SearchState()
            return
        }

        searchJob = viewModelScope.launch {
            delay(1000) // Debounce 1 giây
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val response = RetrofitClient.apiService.searchAddress(apiKey, query)
                val results = response.map { location ->
                    LocationResult(
                        displayName = location.display_name,
                        address = location.address?.values?.joinToString(", ") ?: "Unknown",
                        lat = location.lat.toDouble(),
                        lon = location.lon.toDouble()
                    )
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    results = results
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi khi tìm kiếm: ${e.message}"
                )
            }
        }
    }

    fun openInGoogleMaps(context: Context, result: LocationResult) {
        // Sử dụng URI để hiển thị bản đồ với chỉ đường tiềm năng
        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${result.lat},${result.lon}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        try {
            startActivity(context, mapIntent, null)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${result.lat},${result.lon}"))
            startActivity(context, fallbackIntent, null)
        }
    }
}