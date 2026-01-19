package com.example.sonicflow.di

import android.content.Context
import androidx.room.Room
import com.example.sonicflow.data.local.database.AppDatabase
import com.example.sonicflow.data.local.database.dao.PlaylistDao
import com.example.sonicflow.data.local.database.dao.PlaylistTrackCrossRefDao
import com.example.sonicflow.data.local.database.dao.TrackDao
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
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideTrackDao(database: AppDatabase): TrackDao {
        return database.trackDao()
    }

    @Provides
    @Singleton
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    @Singleton
    fun providePlaylistTrackCrossRefDao(database: AppDatabase): PlaylistTrackCrossRefDao {
        return database.playlistTrackCrossRefDao()
    }
}
