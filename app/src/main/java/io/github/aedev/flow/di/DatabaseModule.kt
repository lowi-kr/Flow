package com.arubr.smsvcodes.di

import android.content.Context
import com.arubr.smsvcodes.data.local.AppDatabase
import com.arubr.smsvcodes.data.local.dao.NotificationDao
import com.arubr.smsvcodes.data.local.dao.PlaylistDao
import com.arubr.smsvcodes.data.local.dao.VideoDao
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
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideVideoDao(database: AppDatabase): VideoDao {
        return database.videoDao()
    }

    @Provides
    fun providePlaylistDao(database: AppDatabase): PlaylistDao {
        return database.playlistDao()
    }

    @Provides
    fun provideNotificationDao(database: AppDatabase): NotificationDao {
        return database.notificationDao()
    }

    @Provides
    fun provideCacheDao(database: AppDatabase): com.arubr.smsvcodes.data.local.dao.CacheDao {
        return database.cacheDao()
    }

    @Provides
    fun provideDownloadDao(database: AppDatabase): com.arubr.smsvcodes.data.local.dao.DownloadDao {
        return database.downloadDao()
    }
}
