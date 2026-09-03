package com.rozhak.imageoptimize.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rozhak.imageoptimize.core.database.entity.TelemetryHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for interacting with the local telemetry database.
 * Provides synchronized persistence for optimization history logs.
 */
@Dao
interface TelemetryDao {
    /**
     * Inserts a new telemetry record. Replaces an existing record if a primary key conflict occurs.
     *
     * @param telemetry The [TelemetryHistoryEntity] object containing the optimization metrics.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTelemetry(telemetry: TelemetryHistoryEntity)

    /**
     * Retrieves a reactive stream of all telemetry records, ordered chronologically (newest first).
     *
     * @return A Room [Flow] that automatically emits a new list upon database modification.
     */
    @Query("SELECT * FROM telemetry_history ORDER BY timestamp DESC")
    fun getAllTelemetryHistory(): Flow<List<TelemetryHistoryEntity>>

    /**
     * Truncates the entire `telemetry_history` table securely.
     */
    @Query("DELETE FROM telemetry_history")
    suspend fun clearHistory()
}
