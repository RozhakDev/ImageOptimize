package com.rozhak.imageoptimize.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.rozhak.imageoptimize.core.database.dao.TelemetryDao
import com.rozhak.imageoptimize.core.database.entity.TelemetryHistoryEntity

@Database(
    entities = [TelemetryHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun telemetryDao(): TelemetryDao
}
