package com.example.vantink.data.local.dao

import androidx.room.*
import com.example.vantink.data.local.entity.DownloadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY chapterNumber ASC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE webtoonId = :webtoonId")
    fun getDownloadsForWebtoon(webtoonId: String): Flow<List<DownloadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: DownloadEntity)

    @Update
    suspend fun updateDownload(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE chapterId = :chapterId")
    suspend fun deleteDownload(chapterId: String)

    @Query("SELECT * FROM downloads WHERE chapterId = :chapterId")
    suspend fun getDownload(chapterId: String): DownloadEntity?
}
