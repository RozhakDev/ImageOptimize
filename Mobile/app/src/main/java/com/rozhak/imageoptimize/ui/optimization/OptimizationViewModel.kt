package com.rozhak.imageoptimize.ui.optimization

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rozhak.imageoptimize.core.model.ImageMetrics
import com.rozhak.imageoptimize.core.model.Result
import com.rozhak.imageoptimize.core.data.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

/**
 * Represents the entire state of the Image Optimization screen.
 * Encapsulates the UI state, ongoing async operations, and selected configuration parameters.
 */
data class OptimizationState(
    val selectedImageUri: Uri? = null,
    val selectedImageFile: File? = null,

    val isAnalyzing: Boolean = false,
    val imageMetrics: ImageMetrics? = null,

    val isOptimizing: Boolean = false,
    val optimizedUrl: String? = null,

    val error: String? = null,

    val targetKb: String = "",
    val resizeWidth: String = "",
    val resizeHeight: String = "",
    val outputFormat: String = "auto",
    val preset: String = "custom",
    val smartCrop: Boolean = false,
    val preserveMetadata: Boolean = false
)

/**
 * Orchestrates the image optimization business logic for the presentation layer.
 * Maintains the single source of truth for the optimization configuration parameters
 * and coordinates interactions between local file storage and the backend compression engine.
 *
 * @property imageRepository Handles communication with remote compression services and local telemetry persistence.
 */
@HiltViewModel
class OptimizationViewModel @Inject constructor(
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OptimizationState())
    val uiState: StateFlow<OptimizationState> = _uiState.asStateFlow()

    /**
     * Initiates the image analysis pipeline when a user selects an image from the gallery.
     * Caches the original image securely into local storage before triggering the remote analysis.
     *
     * @param context Application context required to resolve content URI.
     * @param uri The standard content URI provided by the system's Photo Picker.
     */
    fun onImageSelected(context: Context, uri: Uri) {
        _uiState.update { it.copy(selectedImageUri = uri, error = null, optimizedUrl = null) }

        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzing = true) }

            val file = copyUriToFile(context, uri)
            if (file == null) {
                _uiState.update { it.copy(isAnalyzing = false, error = "Gagal memproses berkas gambar.") }
                return@launch
            }

            _uiState.update { it.copy(selectedImageFile = file) }

            when (val result = imageRepository.analyzeImage(file)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isAnalyzing = false, imageMetrics = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isAnalyzing = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun updateTargetKb(value: String) = _uiState.update { it.copy(targetKb = value) }
    fun updateResizeWidth(value: String) = _uiState.update { it.copy(resizeWidth = value) }
    fun updateResizeHeight(value: String) = _uiState.update { it.copy(resizeHeight = value) }
    fun updateFormat(format: String) = _uiState.update { it.copy(outputFormat = format) }
    fun updatePreset(preset: String) = _uiState.update { it.copy(preset = preset) }
    fun updateSmartCrop(enabled: Boolean) = _uiState.update { it.copy(smartCrop = enabled) }
    fun updatePreserveMetadata(enabled: Boolean) = _uiState.update { it.copy(preserveMetadata = enabled) }

    /**
     * Executes the image compression process based on the current [OptimizationState].
     * Dispatches the network call asynchronously and updates the UI state reactively with either
     * the successfully compressed image URL or a specific domain error.
     */
    fun optimizeImage() {
        val currentState = _uiState.value
        val file = currentState.selectedImageFile
        if (file == null) {
            _uiState.update { it.copy(error = "Silakan pilih gambar terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isOptimizing = true, error = null, optimizedUrl = null) }

            val targetSize = currentState.targetKb.toIntOrNull()
            val rWidth = currentState.resizeWidth.toIntOrNull()
            val rHeight = currentState.resizeHeight.toIntOrNull()

            when (val result = imageRepository.optimizeImage(
                imageFile = file,
                targetSizeKb = targetSize,
                resizeWidth = rWidth,
                resizeHeight = rHeight,
                outputFormat = currentState.outputFormat,
                preset = currentState.preset,
                smartCrop = currentState.smartCrop,
                preserveMetadata = currentState.preserveMetadata
            )) {
                is Result.Success -> {
                    _uiState.update { it.copy(isOptimizing = false, optimizedUrl = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isOptimizing = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    /**
     * Downloads the final optimized image from the server and commits it to the device's
     * persistent MediaStore gallery.
     *
     * Integrates with Android 10+ Scoped Storage (MediaStore.Images.Media.EXTERNAL_CONTENT_URI).
     *
     * @param context Application context used for ContentResolver operations.
     */
    fun saveToGallery(context: Context) {
        val finalUrl = _uiState.value.optimizedUrl ?: return
        
        val extension = if (_uiState.value.outputFormat == "auto") "jpg" else _uiState.value.outputFormat
        val fileName = "opt_${System.currentTimeMillis()}.$extension"
        val mimeType = "image/$extension"

        Toast.makeText(context, "Menyimpan gambar ke Galeri perangkat...", Toast.LENGTH_SHORT).show()

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val imageBytes = java.net.URL(finalUrl).openStream().use { it.readBytes() }

                    val resolver = context.contentResolver
                    val contentValues = android.content.ContentValues().apply {
                        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, mimeType)
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, android.os.Environment.DIRECTORY_PICTURES + "/ImageOptimize")
                            put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
                        }
                    }

                    val uri = resolver.insert(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(imageBytes)
                        }

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            contentValues.clear()
                            contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                            resolver.update(uri, contentValues, null, null)
                        }

                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Gambar berhasil disimpan ke Galeri.", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _uiState.update { it.copy(error = "Gagal mengakses ruang penyimpanan perangkat.") }
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        _uiState.update { it.copy(error = "Gagal memproses: ${e.message}") }
                        Toast.makeText(context, "Kesalahan sistem: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Securely copies the selected image from the external content provider into the app's internal cache directory.
     * This isolates the working file and guarantees that the system won't revoke URI permissions during long operations.
     *
     * @param context Application context.
     * @param uri The external content URI.
     * @return A standard [File] object pointing to the cached data, or null if the I/O operation fails.
     */
    private suspend fun copyUriToFile(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val tempFile = File.createTempFile("opt_raw_", ".tmp", context.cacheDir)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
