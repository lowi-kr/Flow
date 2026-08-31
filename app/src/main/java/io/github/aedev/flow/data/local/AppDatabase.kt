package com.arubr.smsvcodes.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import com.arubr.smsvcodes.data.local.dao.CacheDao
import com.arubr.smsvcodes.data.local.dao.DownloadDao
import com.arubr.smsvcodes.data.local.dao.HomeFeedCacheDao
import com.arubr.smsvcodes.data.local.dao.NotificationDao
import com.arubr.smsvcodes.data.local.dao.PlaylistDao
import com.arubr.smsvcodes.data.local.dao.RecognitionHistoryDao
import com.arubr.smsvcodes.data.local.dao.SubscriptionGroupDao
import com.arubr.smsvcodes.data.local.dao.SyncLogDao
import com.arubr.smsvcodes.data.local.dao.SyncPeerDao
import com.arubr.smsvcodes.data.local.dao.VideoDao
import com.arubr.smsvcodes.data.local.dao.WatchHistoryDao
import com.arubr.smsvcodes.data.local.entity.DownloadEntity
import com.arubr.smsvcodes.data.local.entity.DownloadItemEntity
import com.arubr.smsvcodes.data.local.entity.HomeFeedCacheEntity
import com.arubr.smsvcodes.data.local.entity.MusicHomeCacheEntity
import com.arubr.smsvcodes.data.local.entity.MusicHomeChipEntity
import com.arubr.smsvcodes.data.local.entity.NotificationEntity
import com.arubr.smsvcodes.data.local.entity.PlaylistEntity
import com.arubr.smsvcodes.data.local.entity.PlaylistVideoCrossRef
import com.arubr.smsvcodes.data.local.entity.RecognitionHistoryEntity
import com.arubr.smsvcodes.data.local.entity.SubscriptionFeedEntity
import com.arubr.smsvcodes.data.local.entity.SubscriptionGroupEntity
import com.arubr.smsvcodes.data.local.entity.SyncLogEntity
import com.arubr.smsvcodes.data.local.entity.SyncPeerEntity
import com.arubr.smsvcodes.data.local.entity.VideoEntity
import com.arubr.smsvcodes.data.local.entity.WatchHistoryEntity
import com.arubr.smsvcodes.data.local.migrations.MIGRATIONS
import com.arubr.smsvcodes.data.local.migrations.Migration24To25

@Database(
    entities = [
        VideoEntity::class,
        PlaylistEntity::class,
        PlaylistVideoCrossRef::class,
        NotificationEntity::class,
        SubscriptionFeedEntity::class,
        MusicHomeCacheEntity::class,
        MusicHomeChipEntity::class,
        DownloadEntity::class,
        DownloadItemEntity::class,
        WatchHistoryEntity::class,
        HomeFeedCacheEntity::class,
        SubscriptionGroupEntity::class,
        RecognitionHistoryEntity::class,
        SyncLogEntity::class,
        SyncPeerEntity::class,
    ],
    autoMigrations = [
        AutoMigration(from = 24, to = 25, spec = Migration24To25::class),
    ],
    version = 25,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    abstract fun playlistDao(): PlaylistDao

    abstract fun notificationDao(): NotificationDao

    abstract fun cacheDao(): CacheDao

    abstract fun downloadDao(): DownloadDao

    abstract fun watchHistoryDao(): WatchHistoryDao

    abstract fun homeFeedCacheDao(): HomeFeedCacheDao

    abstract fun subscriptionGroupDao(): SubscriptionGroupDao

    abstract fun recognitionHistoryDao(): RecognitionHistoryDao

    abstract fun syncLogDao(): SyncLogDao

    abstract fun syncPeerDao(): SyncPeerDao

    companion object {
        @Volatile
        @Suppress("ktlint:standard:property-naming")
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance =
                    databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "flow_database",
                    ).addMigrations(*MIGRATIONS)
                        .fallbackToDestructiveMigration(false)
                        .build()
                INSTANCE = instance
                instance
            }
    }
}
