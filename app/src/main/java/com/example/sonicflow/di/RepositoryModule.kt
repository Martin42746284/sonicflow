package com.example.sonicflow.di

import com.example.sonicflow.data.local.preferences.PreferencesManager
import com.example.sonicflow.data.repository.MediaRepositoryImpl
import com.example.sonicflow.data.repository.PlayerRepositoryImpl
import com.example.sonicflow.data.repository.PlaylistRepositoryImpl
import com.example.sonicflow.data.repository.TrackRepositoryImpl
import com.example.sonicflow.domain.repository.MediaRepository
import com.example.sonicflow.domain.repository.PlayerRepository
import com.example.sonicflow.domain.repository.PlaylistRepository
import com.example.sonicflow.domain.repository.TrackRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    // ========== PlayerRepository ==========

    @Provides
    @Singleton
    fun providePlayerRepositoryImpl(
        preferencesManager: PreferencesManager
    ): PlayerRepositoryImpl {
        return PlayerRepositoryImpl(preferencesManager)
    }

    @Provides
    @Singleton
    fun providePlayerRepository(
        impl: PlayerRepositoryImpl
    ): PlayerRepository = impl

    // ========== TrackRepository ==========

    @Provides
    @Singleton
    fun provideTrackRepository(
        impl: TrackRepositoryImpl
    ): TrackRepository = impl

    // ========== MediaRepository ==========

    @Provides
    @Singleton
    fun provideMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository = impl

    // ========== PlaylistRepository ========== ✅ AJOUT

    @Provides
    @Singleton
    fun providePlaylistRepository(
        impl: PlaylistRepositoryImpl
    ): PlaylistRepository = impl
}