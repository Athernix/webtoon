package com.example.vantink.data.local.dao

import androidx.room.*
import com.example.vantink.data.local.entity.RepositoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RepositoryDao {
    @Query("SELECT * FROM extension_repositories")
    fun getAllRepositories(): Flow<List<RepositoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRepository(repo: RepositoryEntity)

    @Delete
    suspend fun deleteRepository(repo: RepositoryEntity)
}
