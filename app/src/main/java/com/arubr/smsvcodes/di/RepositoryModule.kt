package com.arubr.smsvcodes.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.repository.YouTubeRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Singleton
    fun provideYouTubeRepository(playerPreferences: PlayerPreferences): YouTubeRepository = YouTubeRepository.getInstance(playerPreferences)

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.SubscriptionRepository =
        com.arubr.smsvcodes.data.local.SubscriptionRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideLikedVideosRepository(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.LikedVideosRepository =
        com.arubr.smsvcodes.data.local.LikedVideosRepository
            .getInstance(context)

    @Provides
    @Singleton
    fun provideViewHistory(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.ViewHistory =
        com.arubr.smsvcodes.data.local.ViewHistory
            .getInstance(context)

    @Provides
    @Singleton
    fun provideHomeFeedCacheRepository(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.HomeFeedCacheRepository =
        com.arubr.smsvcodes.data.local
            .HomeFeedCacheRepository(context)

    @Provides
    @Singleton
    fun provideMusicPlaylistRepository(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.music.PlaylistRepository =
        com.arubr.smsvcodes.data.music
            .PlaylistRepository(context)

    // VideoDownloadManager is now @Singleton @Inject — Hilt provides it automatically
    @Provides
    @Singleton
    fun providePlayerPreferences(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.PlayerPreferences =
        com.arubr.smsvcodes.data.local
            .PlayerPreferences(context)

    /**
     * Transitional: [com.arubr.smsvcodes.data.local.BackupRepository] still builds its own
     * collaborators, so it is provided here rather than injected through its constructor. One
     * instance serves the whole app.
     */
    @Provides
    @Singleton
    fun provideBackupRepository(
        @ApplicationContext context: Context,
    ): com.arubr.smsvcodes.data.local.BackupRepository =
        com.arubr.smsvcodes.data.local
            .BackupRepository(context)
}
