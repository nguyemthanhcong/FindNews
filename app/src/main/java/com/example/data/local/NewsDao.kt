package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM articles WHERE topicQuery = :topic ORDER BY pubDateTimestamp DESC, id DESC")
    fun getArticlesByTopic(topic: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE isBookmarked = 1 ORDER BY id DESC")
    fun getBookmarkedArticles(): Flow<List<ArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity): Long

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE id = :id")
    suspend fun setBookmark(id: Long, isBookmarked: Boolean)

    @Query("UPDATE articles SET isBookmarked = :isBookmarked WHERE link = :link")
    suspend fun setBookmarkByLink(link: String, isBookmarked: Boolean)

    @Query("SELECT isBookmarked FROM articles WHERE link = :link LIMIT 1")
    suspend fun isBookmarked(link: String): Boolean?

    @Query("DELETE FROM articles WHERE topicQuery = :topic AND isBookmarked = 0")
    suspend fun clearNonBookmarkedArticlesForTopic(topic: String)

    @Query("SELECT * FROM scanned_topics ORDER BY lastScannedAt DESC")
    fun getAllTopics(): Flow<List<ScannedTopicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTopic(topicEntity: ScannedTopicEntity)

    @Query("DELETE FROM scanned_topics WHERE topic = :topic")
    suspend fun deleteTopic(topic: String)

    @Query("DELETE FROM articles WHERE topicQuery = :topic")
    suspend fun deleteAllArticlesForTopic(topic: String)
}
