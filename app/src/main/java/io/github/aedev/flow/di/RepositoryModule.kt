package com.arubr.smsvcodes.di

import android.content.Context
import com.arubr.smsvcodes.data.local.PlayerPreferences
import com.arubr.smsvcodes.data.repository.YouTubeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideYouTubeRepository(playerPreferences: PlayerPreferences): YouTubeRepository {
        return YouTubeRepository.getInstance(playerPreferences)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(@ApplicationContext context: Context): com.arubr.smsvcodes.data.local.SubscriptionRepository {
        return com.arubr.smsvcodes.data.local.SubscriptionRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideLikedVideosRepository(@ApplicationContext context: Context): com.arubr.smsvcodes.data.local.LikedVideosRepository {
        return com.arubr.smsvcodes.data.local.LikedVideosRepository.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideViewHistory(@ApplicationContext context: Context): com.arubr.smsvcodes.data.local.ViewHistory {
        return com.arubr.smsvcodes.data.local.ViewHistory.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideMusicPlaylistRepository(@ApplicationContext context: Context): com.arubr.smsvcodes.data.music.PlaylistRepository {
        return com.arubr.smsvcodes.data.music.PlaylistRepository(context)
    }


    // VideoDownloadManager is now @Singleton @Inject — Hilt provides it automatically
    @Provides
    @Singleton
    fun providePlayerPreferences(@ApplicationContext context: Context): com.arubr.smsvcodes.data.local.PlayerPreferences {
        return com.arubr.smsvcodes.data.local.PlayerPreferences(context)
    }

    @Provides
    @Singleton
    fun provideShortsRepository(@ApplicationContext context: Context): com.arubr.smsvcodes.data.shorts.ShortsRepository {
        return com.arubr.smsvcodes.data.shorts.ShortsRepository.getInstance(context)
    }
}
