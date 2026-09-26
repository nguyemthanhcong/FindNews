package com.example.data.model

data class NewsArticle(
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
)

data class TopicSummary(
    val topic: String,
    val lastScannedAt: Long,
    val totalArticles: Int,
    val totalNewspapers: Int,
    val newspapers: List<String> = emptyList()
)

data class AiSynthesisResult(
    val topic: String,
    val overview: String,
    val newspaperPerspectives: List<NewspaperPerspective>,
    val timelineEvents: List<TimelineEvent>,
    val keyDebates: List<String>,
    val conclusion: String
)

data class NewspaperPerspective(
    val newspaperGroup: String, // e.g., "Nhóm Báo Y tế & Sức khỏe", "Nhóm Báo Pháp luật & Hải quan"
    val newspapers: List<String>,
    val mainAngle: String,
    val highlights: List<String>
)

data class TimelineEvent(
    val dateStr: String,
    val newspaper: String,
    val headline: String,
    val keyTakeaway: String
)
