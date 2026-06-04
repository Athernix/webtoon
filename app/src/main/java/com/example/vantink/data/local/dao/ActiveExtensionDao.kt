package com.example.vantink.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.vantink.data.local.entity.ActiveExtensionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveExtensionDao {
    @Query("SELECT * FROM ActiveExtensions ORDER BY installedAt DESC")
    fun observeActiveExtensions(): Flow<List<ActiveExtensionEntity>>

    @Query("SELECT * FROM ActiveExtensions ORDER BY installedAt DESC")
    suspend fun getActiveExtensions(): List<ActiveExtensionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(extension: ActiveExtensionEntity)

    @Delete
    suspend fun delete(extension: ActiveExtensionEntity)

    @Query("DELETE FROM ActiveExtensions WHERE pkgName = :pkgName")
    suspend fun deleteByPackage(pkgName: String)
}
