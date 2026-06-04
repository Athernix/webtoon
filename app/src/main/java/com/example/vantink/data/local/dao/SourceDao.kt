package com.example.vantink.data.local.dao

import androidx.room.*
import com.example.vantink.data.local.entity.SourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SourceDao {
    @Query("SELECT * FROM sources")
    fun getAllSources(): Flow<List<SourceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: SourceEntity)

    @Update
    suspend fun updateSource(source: SourceEntity)

    @Delete
    suspend fun deleteSource(source: SourceEntity)

    @Query("SELECT * FROM sources WHERE isEnabled = 1")
    suspend fun getActiveSources(): List<SourceEntity>
}
