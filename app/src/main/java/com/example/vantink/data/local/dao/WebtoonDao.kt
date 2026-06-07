package com.example.vantink.data.local.dao

import androidx.room.*
import com.example.vantink.data.local.entity.FavoriteEntity
import com.example.vantink.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WebtoonDao {

    // Favorites
    @Query("SELECT * FROM favorites ORDER BY addedDate DESC")
    fun getFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE contentType = :contentType ORDER BY addedDate DESC")
    fun getFavoritesByContentType(contentType: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE id = :webtoonId)")
    fun isFavorite(webtoonId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE id = :webtoonId")
    suspend fun getFavoriteById(webtoonId: String): FavoriteEntity?

    // History
    @Query("SELECT * FROM history ORDER BY lastReadDate DESC")
    fun getHistory(): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE contentType = :contentType ORDER BY lastReadDate DESC")
    fun getHistoryByContentType(contentType: String): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM history WHERE webtoonId = :webtoonId")
    suspend fun getHistoryForWebtoon(webtoonId: String): HistoryEntity?

    @Query("DELETE FROM history WHERE webtoonId = :webtoonId")
    suspend fun deleteHistory(webtoonId: String)

    @Query("DELETE FROM history")
    suspend fun clearHistory()

    @Query("UPDATE history SET scrollPosition = :scrollPosition, lastReadDate = :lastReadDate WHERE webtoonId = :webtoonId")
    suspend fun updateScrollPosition(webtoonId: String, scrollPosition: Int, lastReadDate: Long = System.currentTimeMillis())

    @Query("SELECT chapterId FROM history WHERE webtoonId = :webtoonId LIMIT 1")
    suspend fun getLastChapterId(webtoonId: String): String?
}
