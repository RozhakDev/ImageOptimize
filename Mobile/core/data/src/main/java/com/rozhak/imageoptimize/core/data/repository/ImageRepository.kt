package com.rozhak.imageoptimize.core.data.repository

import com.rozhak.imageoptimize.core.model.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Core domain boundary for image operations.
 * Serves as the Single Source of Truth for remote optimization services and local telemetry persistence.
 */
interface ImageRepository {
    /**
     * Uploads an image file to the backend service to extract its binary specifications (resolution, format, bands).
     *
     * @param imageFile The physical file to be analyzed. Must be a valid image format.
     * @return A sealed [Result] containing [ImageMetrics] on success, or a descriptive error message on failure.
     */
    suspend fun analyzeImage(imageFile: File): Result<com.rozhak.imageoptimize.core.model.ImageMetrics>

    /**
     * Executes the heavy compression algorithm on the remote backend based on specified parameters.
     * Intercepts the response to normalize relative URLs into absolute URLs and logs the transaction
     * into the local telemetry database if successful.
     *
     * @param imageFile The source image file to be compressed.
     * @param targetSizeKb The desired maximum output file size in Kilobytes, or null for no size limit.
     * @param resizeWidth The target width in pixels for scaling, or null to preserve original width.
     * @param resizeHeight The target height in pixels for scaling, or null to preserve original height.
     * @param outputFormat The desired encoder format (e.g., "auto", "webp", "jpeg").
     * @param preset A predefined compression strategy (e.g., "balanced", "high_quality").
     * @param smartCrop If true, delegates cropping logic to an AI attention model to preserve primary subjects.
     * @param preserveMetadata If true, retains original EXIF data (GPS, camera model) in the output file.
     * @return A sealed [Result] containing the absolute download URL of the optimized image on success.
     */
    suspend fun optimizeImage(
        imageFile: File,
        targetSizeKb: Int?,
        resizeWidth: Int?,
        resizeHeight: Int?,
        outputFormat: String,
        preset: String,
        smartCrop: Boolean,
        preserveMetadata: Boolean
    ): Result<String>

    /**
     * Provides a continuous stream of telemetry records from the local database.
     * Emits a new list every time an insertion or deletion occurs in the database.
     *
     * @return A reactive [Flow] containing a list of all historical [TelemetryHistory] entries.
     */
    fun getTelemetryHistory(): Flow<List<com.rozhak.imageoptimize.core.model.TelemetryHistory>>
    
    /**
     * Purges all telemetry records from the local storage.
     * This operation is irreversible.
     */
    suspend fun clearTelemetryHistory()
}
