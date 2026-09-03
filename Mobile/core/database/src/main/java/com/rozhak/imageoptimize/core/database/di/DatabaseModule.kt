package com.rozhak.imageoptimize.core.database.di

import android.content.Context
import android.text.method.SingleLineTransformationMethod
import androidx.room.Room
import com.rozhak.imageoptimize.core.database.AppDatabase
import com.rozhak.imageoptimize.core.database.dao.TelemetryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "image_optimize_telemetry.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTelemetryDao(database: AppDatabase): TelemetryDao {
        return database.telemetryDao()
    }
}
