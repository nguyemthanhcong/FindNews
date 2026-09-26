package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.NewsArticle

@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["topicQuery"]),
        Index(value = ["link"], unique = true)
    ]
)
data class ArticleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val topicQuery: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val pubDateTimestamp: Long = 0L,
    val newspaper: String,
    val link: String,
    val sourceUrl: String,
    val imageUrl: String? = null,
    val isBookmarked: Boolean = false,
    val scannedAt: Long = System.currentTimeMillis(),
    val perspectiveTag: String = "Tổng hợp"
) {
    fun toModel(): NewsArticle = NewsArticle(
        id = id,
        topicQuery = topicQuery,
        title = title,
        description = description,
        pubDate = pubDate,
        pubDateTimestamp = pubDateTimestamp,
        newspaper = newspaper,
        link = link,
        sourceUrl = sourceUrl,
        imageUrl = imageUrl,
        isBookmarked = isBookmarked,
        scannedAt = scannedAt,
        perspectiveTag = perspectiveTag
    )

    companion object {
        fun fromModel(model: NewsArticle): ArticleEntity = ArticleEntity(
            id = model.id,
            topicQuery = model.topicQuery,
            title = model.title,
            description = model.description,
            pubDate = model.pubDate,
            pubDateTimestamp = model.pubDateTimestamp,
            newspaper = model.newspaper,
            link = model.link,
            sourceUrl = model.sourceUrl,
            imageUrl = model.imageUrl,
            isBookmarked = model.isBookmarked,
            scannedAt = model.scannedAt,
            perspectiveTag = model.perspectiveTag
        )
    }
}

@Entity(
    tableName = "scanned_topics",
    indices = [Index(value = ["topic"], unique = true)]
)
data class ScannedTopicEntity(
    @PrimaryKey
    val topic: String,
    val lastScannedAt: Long = System.currentTimeMillis(),
    val totalArticles: Int = 0,
    val totalNewspapers: Int = 0,
    val newspaperNames: String = "" // Comma-separated newspaper names
)
