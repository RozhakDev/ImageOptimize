package com.rozhak.imageoptimize.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room database entity representing a single image optimization transaction.
 * Stores crucial telemetry data such as file size comparisons and execution times.
 */
@Entity(tableName = "telemetry_history")
data class TelemetryHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "original_url")
    val originalUrl: String,

    @ColumnInfo(name = "optimized_url")
    val optimizedUrl: String,

    @ColumnInfo(name = "original_size_bytes")
    val originalSizeBytes: Long,

    @ColumnInfo(name = "optimized_size_bytes")
    val optimizedSizeBytes: Long,

    @ColumnInfo(name = "compression_ratio")
    val compressionRatio: String,

    @ColumnInfo(name = "processing_time_ms")
    val processingTimeMs: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis()
)
