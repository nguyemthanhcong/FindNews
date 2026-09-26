package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AiSynthesisResult
import com.example.data.model.NewsArticle
import com.example.data.model.TopicSummary
import com.example.data.repository.NewsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    HOME,
    BOOKMARKS,
    TOPIC_HISTORY
}

enum class SortOption(val displayName: String) {
    NEWEST("Mới nhất"),
    OLDEST("Cũ nhất"),
    BY_NEWSPAPER("Theo tòa soạn báo")
}

enum class DateFilterOption(val displayName: String) {
    ALL("Tất cả thời gian"),
    TODAY("24 giờ qua"),
    WEEK("7 ngày qua"),
    MONTH("30 ngày qua"),
    CUSTOM("Chọn ngày cụ thể")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = NewsRepository(database.newsDao())

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("Thuốc lá nung nóng")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeTopic = MutableStateFlow("Thuốc lá nung nóng")
    val activeTopic: StateFlow<String> = _activeTopic.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isSynthesizing = MutableStateFlow(false)
    val isSynthesizing: StateFlow<Boolean> = _isSynthesizing.asStateFlow()

    private val _synthesisResult = MutableStateFlow<AiSynthesisResult?>(null)
    val synthesisResult: StateFlow<AiSynthesisResult?> = _synthesisResult.asStateFlow()

    // Filters
    private val _selectedNewspaperFilter = MutableStateFlow<String?>(null)
    val selectedNewspaperFilter: StateFlow<String?> = _selectedNewspaperFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _selectedDateFilter = MutableStateFlow(DateFilterOption.ALL)
    val selectedDateFilter: StateFlow<DateFilterOption> = _selectedDateFilter.asStateFlow()

    private val _customDateMillis = MutableStateFlow<Long?>(null)
    val customDateMillis: StateFlow<Long?> = _customDateMillis.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _activeWebsiteArticle = MutableStateFlow<NewsArticle?>(null)
    val activeWebsiteArticle: StateFlow<NewsArticle?> = _activeWebsiteArticle.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val articles: StateFlow<List<NewsArticle>> = _activeTopic
        .flatMapLatest { topic -> repository.getArticlesForTopic(topic) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarkedArticles: StateFlow<List<NewsArticle>> = repository.getBookmarkedArticles()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val scannedTopics: StateFlow<List<TopicSummary>> = repository.getScannedTopics()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private data class FilterCriteria(
        val newspaper: String?,
        val category: String?,
        val dateOption: DateFilterOption,
        val customDateMillis: Long?,
        val sort: SortOption
    )

    private val filterCriteria = combine(
        _selectedNewspaperFilter,
        _selectedCategoryFilter,
        _selectedDateFilter,
        _customDateMillis,
        _sortOption
    ) { paper, category, date, custom, sort ->
        FilterCriteria(paper, category, date, custom, sort)
    }

    val filteredArticles: StateFlow<List<NewsArticle>> = combine(
        articles,
        filterCriteria
    ) { allArticles, criteria ->
        var result = allArticles

        // 1. Filter by Newspaper
        if (!criteria.newspaper.isNullOrBlank()) {
            result = result.filter { it.newspaper.equals(criteria.newspaper, ignoreCase = true) }
        }

        // 2. Filter by Category / Thể loại
        if (!criteria.category.isNullOrBlank() && criteria.category != "Tất cả thể loại") {
            result = result.filter { it.perspectiveTag.equals(criteria.category, ignoreCase = true) }
        }

        // 3. Filter by Date / Ngày tháng năm
        val now = System.currentTimeMillis()
        val dayMs = 86_400_000L

        result = when (criteria.dateOption) {
            DateFilterOption.ALL -> result
            DateFilterOption.TODAY -> result.filter {
                val diff = now - it.pubDateTimestamp
                diff in 0..dayMs || it.pubDate.contains("Hôm nay", ignoreCase = true)
            }
            DateFilterOption.WEEK -> result.filter {
                val diff = now - it.pubDateTimestamp
                diff in 0..(7 * dayMs) || it.pubDate.contains("Hôm nay", ignoreCase = true) || it.pubDate.contains("Hôm qua", ignoreCase = true)
            }
            DateFilterOption.MONTH -> result.filter {
                val diff = now - it.pubDateTimestamp
                diff in 0..(30 * dayMs)
            }
            DateFilterOption.CUSTOM -> {
                val customDate = criteria.customDateMillis
                if (customDate != null) {
                    val targetCal = Calendar.getInstance().apply { timeInMillis = customDate }
                    val targetYear = targetCal.get(Calendar.YEAR)
                    val targetMonth = targetCal.get(Calendar.MONTH)
                    val targetDay = targetCal.get(Calendar.DAY_OF_MONTH)

                    result.filter { article ->
                        val artCal = Calendar.getInstance().apply { timeInMillis = article.pubDateTimestamp }
                        artCal.get(Calendar.YEAR) == targetYear &&
                            artCal.get(Calendar.MONTH) == targetMonth &&
                            artCal.get(Calendar.DAY_OF_MONTH) == targetDay
                    }
                } else result
            }
        }

        // 4. Sort
        when (criteria.sort) {
            SortOption.NEWEST -> result.sortedByDescending { it.pubDateTimestamp }
            SortOption.OLDEST -> result.sortedBy { it.pubDateTimestamp }
            SortOption.BY_NEWSPAPER -> result.sortedBy { it.newspaper }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Automatically perform first scan for "Thuốc lá nung nóng" on startup
        scanTopic("Thuốc lá nung nóng")
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun scanTopic(topicToScan: String? = null) {
        val topic = (topicToScan ?: _searchQuery.value).trim()
        if (topic.isBlank()) return

        _activeTopic.value = topic
        _searchQuery.value = topic
        _selectedNewspaperFilter.value = null
        _selectedCategoryFilter.value = null
        _selectedDateFilter.value = DateFilterOption.ALL
        _customDateMillis.value = null
        _errorMessage.value = null
        _isScanning.value = true

        viewModelScope.launch {
            try {
                repository.scanAndSaveTopic(topic)
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi khi quét báo: ${e.localizedMessage}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun toggleBookmark(article: NewsArticle) {
        viewModelScope.launch {
            repository.toggleBookmark(article)
        }
    }

    fun generateAiSynthesis() {
        val currentArticles = articles.value
        val topic = _activeTopic.value
        if (currentArticles.isEmpty()) {
            _errorMessage.value = "Chưa có bài viết nào để phân tích."
            return
        }

        _isSynthesizing.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            val result = repository.analyzeTopicArticles(topic, currentArticles)
            result.onSuccess { synthesis ->
                _synthesisResult.value = synthesis
            }.onFailure { err ->
                _errorMessage.value = "Lỗi khi tổng hợp: ${err.localizedMessage}"
            }
            _isSynthesizing.value = false
        }
    }

    fun clearSynthesis() {
        _synthesisResult.value = null
    }

    fun openWebsite(article: NewsArticle) {
        _activeWebsiteArticle.value = article
    }

    fun closeWebsite() {
        _activeWebsiteArticle.value = null
    }

    fun setFilterNewspaper(newspaper: String?) {
        _selectedNewspaperFilter.value = if (_selectedNewspaperFilter.value == newspaper) null else newspaper
    }

    fun setFilterCategory(category: String?) {
        _selectedCategoryFilter.value = if (_selectedCategoryFilter.value == category) null else category
    }

    fun setDateFilter(option: DateFilterOption) {
        _selectedDateFilter.value = option
        if (option != DateFilterOption.CUSTOM) {
            _customDateMillis.value = null
        }
    }

    fun setCustomDate(dateMillis: Long?) {
        _customDateMillis.value = dateMillis
        _selectedDateFilter.value = DateFilterOption.CUSTOM
    }

    fun resetAllFilters() {
        _selectedNewspaperFilter.value = null
        _selectedCategoryFilter.value = null
        _selectedDateFilter.value = DateFilterOption.ALL
        _customDateMillis.value = null
    }

    fun setSort(option: SortOption) {
        _sortOption.value = option
    }

    fun setScreen(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun deleteTopic(topic: String) {
        viewModelScope.launch {
            repository.deleteTopic(topic)
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
