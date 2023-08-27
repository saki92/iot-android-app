package com.example.irrigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DeviceData())
    val uiState: StateFlow<DeviceData> = _uiState.asStateFlow()

}