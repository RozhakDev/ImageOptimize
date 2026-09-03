package com.rozhak.imageoptimize.core.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JSendResponse<T>(
    val status: String,
    val message: String,
    val data: T?
)

@JsonClass(generateAdapter = true)
data class HealthDataDto(
    val version: String
)

@JsonClass(generateAdapter = true)
data class AnalysisDataDto(
    val width: Int,
    val height: Int,
    val bands: Int,
    @Json(name = "has_alpha") val hasAlpha: Boolean,
    val format: String
)

@JsonClass(generateAdapter = true)
data class OptimizationDataDto(
    @Json(name = "original_url") val originalUrl: String,
    @Json(name = "optimized_url") val optimizedUrl: String,
    @Json(name = "original_size_bytes") val originalSizeBytes: Long,
    @Json(name = "optimized_size_bytes") val optimizedSizeBytes: Long,
    @Json(name = "compression_ratio") val compressionRatio: String,
    @Json(name = "processing_time_ms") val processingTimeMs: String
)
