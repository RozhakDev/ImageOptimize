package com.rozhak.imageoptimize.core.data.repository

import com.rozhak.imageoptimize.core.database.dao.TelemetryDao
import com.rozhak.imageoptimize.core.database.entity.TelemetryHistoryEntity
import com.rozhak.imageoptimize.core.network.exception.AppException
import com.rozhak.imageoptimize.core.network.service.ImageOptimizeService
import com.rozhak.imageoptimize.core.model.ImageMetrics
import com.rozhak.imageoptimize.core.model.Result
import com.rozhak.imageoptimize.core.data.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Named

/**
 * Concrete implementation of the [ImageRepository] domain interface.
 * Acts as the integration layer between the RESTful [ImageOptimizeService] (Retrofit)
 * and the local SQLite [TelemetryDao] (Room).
 *
 * @property apiService The Retrofit interface for remote network calls.
 * @property telemetryDao The Room DAO for local telemetry persistence.
 * @property baseUrl The injected backend environment URL, used to resolve absolute image paths.
 */
class ImageRepositoryImpl @Inject constructor(
    private val apiService: ImageOptimizeService,
    private val telemetryDao: TelemetryDao,
    @Named("BaseUrl") private val baseUrl: String
) : ImageRepository {
    /**
     * @see ImageRepository.analyzeImage
     */
    override suspend fun analyzeImage(imageFile: File): Result<com.rozhak.imageoptimize.core.model.ImageMetrics> = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val response = apiService.analyzeImage(filePart)

            response.data?.let { dto ->
                Result.Success(com.rozhak.imageoptimize.core.model.ImageMetrics(
                    width = dto.width,
                    height = dto.height,
                    bands = dto.bands,
                    hasAlpha = dto.hasAlpha,
                    format = dto.format
                ))
            } ?: Result.Error("Empty payload from analysis gateway")
        } catch (e: AppException) {
            Result.Error(message = e.message ?: "Analysis Failed")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unknown I/O Exception")
        }
    }

    /**
     * @see ImageRepository.optimizeImage
     * Constructs the multipart payload, executes the network request, normalizes the relative URL,
     * and persists the transaction details into the local telemetry database.
     */
    override suspend fun optimizeImage(
        imageFile: File,
        targetSizeKb: Int?,
        resizeWidth: Int?,
        resizeHeight: Int?,
        outputFormat: String,
        preset: String,
        smartCrop: Boolean,
        preserveMetadata: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)

            val targetKbPart = targetSizeKb?.toString()?.toRequestBody(MultipartBody.FORM)
            val widthPart = resizeWidth?.toString()?.toRequestBody(MultipartBody.FORM)
            val heightPart = resizeHeight?.toString()?.toRequestBody(MultipartBody.FORM)
            val formatPart = outputFormat.toRequestBody(MultipartBody.FORM)
            val presetPart = preset.toRequestBody(MultipartBody.FORM)
            val smartCropPart = smartCrop.toString().toRequestBody(MultipartBody.FORM)
            val preserveMetaPart = preserveMetadata.toString().toRequestBody(MultipartBody.FORM)

            val response = apiService.optimizeImage(
                file = filePart,
                targetSizeKb = targetKbPart,
                resizeWidth = widthPart,
                resizeHeight = heightPart,
                outputFormat = formatPart,
                preset = presetPart,
                smartCrop = smartCropPart,
                preserveMetadata = preserveMetaPart
            )

            response.data?.let { dto ->
                val finalUrl = if (dto.optimizedUrl.startsWith("/")) {
                    val formattedBase = if (baseUrl.endsWith("/")) baseUrl.dropLast(1) else baseUrl
                    "$formattedBase${dto.optimizedUrl}"
                } else {
                    dto.optimizedUrl
                }

                val entity = TelemetryHistoryEntity(
                    originalUrl = dto.originalUrl,
                    optimizedUrl = finalUrl,
                    originalSizeBytes = dto.originalSizeBytes,
                    optimizedSizeBytes = dto.optimizedSizeBytes,
                    compressionRatio = dto.compressionRatio,
                    processingTimeMs = dto.processingTimeMs
                )
                telemetryDao.insertTelemetry(entity)

                return@withContext Result.Success(finalUrl)
            } ?: return@withContext Result.Error("Empty payload from gateway")
        } catch (e: AppException) {
            Result.Error(message = e.message ?: "Unknown Gateway Error")
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unknown I/O Exception")
        }
    }

    /**
     * @see ImageRepository.getTelemetryHistory
     * Maps the underlying DAO's entity flow directly into domain model flow.
     */
    override fun getTelemetryHistory(): Flow<List<com.rozhak.imageoptimize.core.model.TelemetryHistory>> {
        return telemetryDao.getAllTelemetryHistory().map { entities -> 
            entities.map { entity ->
                com.rozhak.imageoptimize.core.model.TelemetryHistory(
                    id = entity.id,
                    originalUrl = entity.originalUrl,
                    optimizedUrl = entity.optimizedUrl,
                    originalSizeBytes = entity.originalSizeBytes,
                    optimizedSizeBytes = entity.optimizedSizeBytes,
                    compressionRatio = entity.compressionRatio,
                    processingTimeMs = entity.processingTimeMs,
                    timestamp = entity.timestamp
                )
            }
        }
    }

    /**
     * @see ImageRepository.clearTelemetryHistory
     */
    override suspend fun clearTelemetryHistory() = withContext(Dispatchers.IO) {
        telemetryDao.clearHistory()
    }
}
