package com.example.data.repository

import com.example.data.ai.GeminiNewsAnalyzer
import com.example.data.local.ArticleEntity
import com.example.data.local.NewsDao
import com.example.data.local.ScannedTopicEntity
import com.example.data.model.AiSynthesisResult
import com.example.data.model.NewsArticle
import com.example.data.model.TopicSummary
import com.example.data.remote.NewsRssScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NewsRepository(
    private val newsDao: NewsDao,
    private val rssScanner: NewsRssScanner = NewsRssScanner(),
    private val aiAnalyzer: GeminiNewsAnalyzer = GeminiNewsAnalyzer()
) {

    fun getArticlesForTopic(topic: String): Flow<List<NewsArticle>> {
        return newsDao.getArticlesByTopic(topic).map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getBookmarkedArticles(): Flow<List<NewsArticle>> {
        return newsDao.getBookmarkedArticles().map { entities ->
            entities.map { it.toModel() }
        }
    }

    fun getScannedTopics(): Flow<List<TopicSummary>> {
        return newsDao.getAllTopics().map { list ->
            list.map {
                val newspapers = if (it.newspaperNames.isNotBlank()) {
                    it.newspaperNames.split(",").map { name -> name.trim() }
                } else emptyList()

                TopicSummary(
                    topic = it.topic,
                    lastScannedAt = it.lastScannedAt,
                    totalArticles = it.totalArticles,
                    totalNewspapers = it.totalNewspapers,
                    newspapers = newspapers
                )
            }
        }
    }

    suspend fun scanAndSaveTopic(topic: String): List<NewsArticle> {
        val cleanTopic = topic.trim()
        if (cleanTopic.isBlank()) return emptyList()

        val scannedArticles = rssScanner.scanNewsForTopic(cleanTopic)
        if (scannedArticles.isNotEmpty()) {
            val entitiesToInsert = scannedArticles.map { article ->
                // Check if already bookmarked
                val isBookmarked = newsDao.isBookmarked(article.link) ?: false
                ArticleEntity.fromModel(article.copy(isBookmarked = isBookmarked))
            }

            newsDao.insertArticles(entitiesToInsert)

            val distinctNewspapers = scannedArticles.map { it.newspaper }.distinct()
            newsDao.insertOrUpdateTopic(
                ScannedTopicEntity(
                    topic = cleanTopic,
                    lastScannedAt = System.currentTimeMillis(),
                    totalArticles = scannedArticles.size,
                    totalNewspapers = distinctNewspapers.size,
                    newspaperNames = distinctNewspapers.joinToString(",")
                )
            )
        }
        return scannedArticles
    }

    suspend fun toggleBookmark(article: NewsArticle) {
        val newBookmarkState = !article.isBookmarked
        if (article.id != 0L) {
            newsDao.setBookmark(article.id, newBookmarkState)
        } else {
            newsDao.setBookmarkByLink(article.link, newBookmarkState)
        }
    }

    suspend fun analyzeTopicArticles(
        topic: String,
        articles: List<NewsArticle>
    ): Result<AiSynthesisResult> {
        return aiAnalyzer.synthesizeCrossPressArticles(topic, articles)
    }

    suspend fun deleteTopic(topic: String) {
        newsDao.deleteTopic(topic)
        newsDao.deleteAllArticlesForTopic(topic)
    }
}
