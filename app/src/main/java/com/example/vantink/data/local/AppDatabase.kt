package com.example.vantink.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.vantink.data.local.dao.ActiveExtensionDao
import com.example.vantink.data.local.dao.WebtoonDao
import com.example.vantink.data.local.entity.ActiveExtensionEntity
import com.example.vantink.data.local.entity.FavoriteEntity
import com.example.vantink.data.local.entity.HistoryEntity

@Database(
    entities = [
        FavoriteEntity::class, 
        HistoryEntity::class, 
        com.example.vantink.data.local.entity.DownloadEntity::class, 
        com.example.vantink.data.local.entity.SourceEntity::class,
        com.example.vantink.data.local.entity.RepositoryEntity::class,
        ActiveExtensionEntity::class
    ],
    version = 12, // Bumped to 12 to resolve "Room cannot verify the data integrity" error
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val webtoonDao: WebtoonDao
    abstract val downloadDao: com.example.vantink.data.local.dao.DownloadDao
    abstract val sourceDao: com.example.vantink.data.local.dao.SourceDao
    abstract val repositoryDao: com.example.vantink.data.local.dao.RepositoryDao
    abstract val activeExtensionDao: ActiveExtensionDao

    companion object {
        const val DATABASE_NAME = "vantink_db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
