package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SynthesisReportSheet
import com.example.ui.components.WebsiteViewerModal
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.TopicHistoryScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
                val activeTopic by viewModel.activeTopic.collectAsStateWithLifecycle()
                val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
                val isSynthesizing by viewModel.isSynthesizing.collectAsStateWithLifecycle()
                val articles by viewModel.articles.collectAsStateWithLifecycle()
                val filteredArticles by viewModel.filteredArticles.collectAsStateWithLifecycle()
                val bookmarkedArticles by viewModel.bookmarkedArticles.collectAsStateWithLifecycle()
                val scannedTopics by viewModel.scannedTopics.collectAsStateWithLifecycle()
                val synthesisResult by viewModel.synthesisResult.collectAsStateWithLifecycle()
                val activeWebsiteArticle by viewModel.activeWebsiteArticle.collectAsStateWithLifecycle()
                val selectedNewspaperFilter by viewModel.selectedNewspaperFilter.collectAsStateWithLifecycle()
                val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
                val selectedDateFilter by viewModel.selectedDateFilter.collectAsStateWithLifecycle()
                val customDateMillis by viewModel.customDateMillis.collectAsStateWithLifecycle()
                val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
                val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(errorMessage) {
                    errorMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearErrorMessage()
                    }
                }

                // Custom back handling for tabs
                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    viewModel.setScreen(AppScreen.HOME)
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        if (activeWebsiteArticle == null) {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.HOME,
                                    onClick = { viewModel.setScreen(AppScreen.HOME) },
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.TravelExplore,
                                            contentDescription = "Quét Báo"
                                        )
                                    },
                                    label = { Text("Quét Báo") },
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.BOOKMARKS,
                                    onClick = { viewModel.setScreen(AppScreen.BOOKMARKS) },
                                    icon = {
                                        if (bookmarkedArticles.isNotEmpty()) {
                                            BadgedBox(badge = {
                                                Badge { Text("${bookmarkedArticles.size}") }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Bookmark,
                                                    contentDescription = "Đã lưu"
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = "Đã lưu"
                                            )
                                        }
                                    },
                                    label = { Text("Đã lưu") },
                                    modifier = Modifier.testTag("nav_bookmarks")
                                )

                                NavigationBarItem(
                                    selected = currentScreen == AppScreen.TOPIC_HISTORY,
                                    onClick = { viewModel.setScreen(AppScreen.TOPIC_HISTORY) },
                                    icon = {
                                        if (scannedTopics.isNotEmpty()) {
                                            BadgedBox(badge = {
                                                Badge { Text("${scannedTopics.size}") }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.History,
                                                    contentDescription = "Lịch sử"
                                                )
                                            }
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.History,
                                                contentDescription = "Lịch sử"
                                            )
                                        }
                                    },
                                    label = { Text("Chủ đề") },
                                    modifier = Modifier.testTag("nav_history")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> {
                                HomeScreen(
                                    viewModel = viewModel,
                                    articles = filteredArticles,
                                    allTopicArticles = articles,
                                    isScanning = isScanning,
                                    isSynthesizing = isSynthesizing,
                                    selectedNewspaperFilter = selectedNewspaperFilter,
                                    selectedCategoryFilter = selectedCategoryFilter,
                                    selectedDateFilter = selectedDateFilter,
                                    customDateMillis = customDateMillis,
                                    sortOption = sortOption,
                                    searchQuery = searchQuery,
                                    activeTopic = activeTopic,
                                    onOpenWebsite = { viewModel.openWebsite(it) },
                                    onToggleBookmark = { viewModel.toggleBookmark(it) },
                                    onTriggerSynthesis = { viewModel.generateAiSynthesis() }
                                )
                            }

                            AppScreen.BOOKMARKS -> {
                                BookmarksScreen(
                                    bookmarkedArticles = bookmarkedArticles,
                                    onOpenWebsite = { viewModel.openWebsite(it) },
                                    onToggleBookmark = { viewModel.toggleBookmark(it) }
                                )
                            }

                            AppScreen.TOPIC_HISTORY -> {
                                TopicHistoryScreen(
                                    topics = scannedTopics,
                                    onSelectTopic = {
                                        viewModel.scanTopic(it)
                                        viewModel.setScreen(AppScreen.HOME)
                                    },
                                    onRescanTopic = {
                                        viewModel.scanTopic(it)
                                        viewModel.setScreen(AppScreen.HOME)
                                    },
                                    onDeleteTopic = { viewModel.deleteTopic(it) }
                                )
                            }
                        }

                        // AI Synthesis Bottom Sheet
                        synthesisResult?.let { result ->
                            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                            SynthesisReportSheet(
                                result = result,
                                sheetState = sheetState,
                                onDismiss = { viewModel.clearSynthesis() }
                            )
                        }

                        // Website Viewer & Inspector Tool
                        activeWebsiteArticle?.let { article ->
                            WebsiteViewerModal(
                                article = article,
                                onDismiss = { viewModel.closeWebsite() }
                            )
                        }
                    }
                }
            }
        }
    }
}
