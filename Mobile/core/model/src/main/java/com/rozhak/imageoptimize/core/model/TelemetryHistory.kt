package com.rozhak.imageoptimize.core.model

data class TelemetryHistory(
    val id: Int,
    val originalUrl: String,
    val optimizedUrl: String,
    val originalSizeBytes: Long,
    val optimizedSizeBytes: Long,
    val compressionRatio: String,
    val processingTimeMs: String,
    val timestamp: Long
)
