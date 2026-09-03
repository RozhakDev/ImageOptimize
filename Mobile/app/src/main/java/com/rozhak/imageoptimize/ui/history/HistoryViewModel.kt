package com.rozhak.imageoptimize.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozhak.imageoptimize.core.data.repository.ImageRepository
import com.rozhak.imageoptimize.core.model.TelemetryHistory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Orchestrates the retrieval and management of optimization telemetry history.
 * Binds the reactive database flow from the repository to the UI lifecycle.
 *
 * @property imageRepository The primary data source for querying and mutating telemetry records.
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    /**
     * A cold flow converted to a hot [StateFlow] that emits the latest list of [TelemetryHistory].
     * Subscribes to the underlying Room database updates automatically.
     * Retains the latest state for 5 seconds after the last subscriber disconnects to handle configuration changes.
     */
    val historyState: StateFlow<List<TelemetryHistory>> = imageRepository.getTelemetryHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
        
    /**
     * Executes a destructive operation to purge all telemetry logs from the local database.
     * Runs asynchronously on the I/O dispatcher.
     */
    fun clearHistory() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            imageRepository.clearTelemetryHistory()
        }
    }
}
