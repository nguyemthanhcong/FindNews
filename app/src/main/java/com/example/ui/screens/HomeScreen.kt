package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle
import com.example.ui.components.ArticleCard
import com.example.ui.components.RadarScanningIndicator
import com.example.ui.components.getNewspaperBrandColor
import com.example.ui.viewmodel.DateFilterOption
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SortOption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val POPULAR_TOPICS = listOf(
    "Thuốc lá nung nóng",
    "Xăng sinh học E5",
    "Vàng miếng SJC",
    "Đường sắt cao tốc",
    "Trí tuệ nhân tạo AI",
    "Thị trường ô tô điện"
)

private val STANDARD_CATEGORIES = listOf(
    "Y tế & Sức khỏe",
    "Chính sách & Pháp lý",
    "Điều tra & Xử lý",
    "Thị trường & Kinh tế",
    "Thời sự & Đời sống"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    articles: List<NewsArticle>,
    allTopicArticles: List<NewsArticle>,
    isScanning: Boolean,
    isSynthesizing: Boolean,
    selectedNewspaperFilter: String?,
    selectedCategoryFilter: String?,
    selectedDateFilter: DateFilterOption,
    customDateMillis: Long?,
    sortOption: SortOption,
    searchQuery: String,
    activeTopic: String,
    onOpenWebsite: (NewsArticle) -> Unit,
    onToggleBookmark: (NewsArticle) -> Unit,
    onTriggerSynthesis: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    var showSortMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    // Distinct newspaper counts
    val newspaperCounts = remember(allTopicArticles) {
        allTopicArticles.groupingBy { it.newspaper }.eachCount()
    }

    // Dynamic categories from scanned data + standard categories
    val allCategories = remember(allTopicArticles) {
        val detected = allTopicArticles.map { it.perspectiveTag }.filter { it.isNotBlank() && it != "Tổng hợp" }
        (STANDARD_CATEGORIES + detected).distinct()
    }

    val categoryCounts = remember(allTopicArticles) {
        allTopicArticles.groupingBy { it.perspectiveTag }.eachCount()
    }

    val hasActiveFilters = selectedNewspaperFilter != null ||
        selectedCategoryFilter != null ||
        selectedDateFilter != DateFilterOption.ALL ||
        customDateMillis != null

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_scroll"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Top Banner & Search Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header Brand
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Newspaper,
                            contentDescription = "Logo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Quét Báo Toàn Diện",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Thu thập & đối chiếu bài viết từ mọi tờ báo",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Input with Action Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Nhập chủ đề (vd: Thuốc lá nung nóng)...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Xóa",
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.scanTopic()
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_text_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.scanTopic()
                        },
                        enabled = !isScanning && searchQuery.isNotBlank(),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("scan_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Quét Báo",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Popular Topic Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gợi ý:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.outline
                    )
                    POPULAR_TOPICS.forEach { topic ->
                        val isSelected = activeTopic.equals(topic, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                viewModel.scanTopic(topic)
                            }
                        ) {
                            Text(
                                text = topic,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Radar Scanning Animation
        if (isScanning) {
            item {
                RadarScanningIndicator(topic = activeTopic)
            }
        }

        // Summary Card & Action Bar
        if (!isScanning && allTopicArticles.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Đã thu thập ${allTopicArticles.size} bài viết",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Từ ${newspaperCounts.size} tòa soạn báo điện tử",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Sort dropdown button
                            Box {
                                OutlinedButton(
                                    onClick = { showSortMenu = true },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = "Sắp xếp",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = sortOption.displayName,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }

                                DropdownMenu(
                                    expanded = showSortMenu,
                                    onDismissRequest = { showSortMenu = false }
                                ) {
                                    SortOption.values().forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option.displayName) },
                                            onClick = {
                                                viewModel.setSort(option)
                                                showSortMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions: AI Multi-newspaper synthesis & Export whole report
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onTriggerSynthesis,
                                enabled = !isSynthesizing,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_synthesis_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                if (isSynthesizing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Đang phân tích...")
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("AI Tổng Hợp Đa Báo")
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    exportAllArticlesToClipboard(context, activeTopic, allTopicArticles)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_report_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Xuất",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Xuất Báo Cáo")
                            }
                        }
                    }
                }
            }

            // FILTER CONTROLS SECTION
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Filter header with Reset button if filters active
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.FilterAlt,
                                    contentDescription = "Bộ lọc",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Bộ Lọc Nâng Cao (${articles.size}/${allTopicArticles.size} bài)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (hasActiveFilters) {
                                TextButton(
                                    onClick = { viewModel.resetAllFilters() },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FilterAltOff,
                                        contentDescription = "Đặt lại",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Xóa lọc",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 1. FILTER THEO NGÀY THÁNG NĂM
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Ngày tháng năm",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lọc theo ngày đăng:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            DateFilterOption.values().forEach { option ->
                                if (option != DateFilterOption.CUSTOM) {
                                    val isSelected = selectedDateFilter == option && customDateMillis == null
                                    ElevatedFilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setDateFilter(option) },
                                        label = { Text(option.displayName) },
                                        modifier = Modifier.testTag("date_filter_${option.name}")
                                    )
                                }
                            }

                            // Custom date button / chip
                            val isCustomSelected = customDateMillis != null || selectedDateFilter == DateFilterOption.CUSTOM
                            val customLabel = if (customDateMillis != null) {
                                "Ngày: ${dateFormatter.format(Date(customDateMillis))}"
                            } else {
                                "Chọn ngày..."
                            }

                            ElevatedFilterChip(
                                selected = isCustomSelected,
                                onClick = { showDatePicker = true },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = "Chọn ngày",
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (customDateMillis != null) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Xóa ngày cụ thể",
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.setCustomDate(null) }
                                        )
                                    }
                                },
                                label = { Text(customLabel) },
                                colors = FilterChipDefaults.elevatedFilterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                modifier = Modifier.testTag("custom_date_filter_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. FILTER THEO THỂ LOẠI
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Thể loại",
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lọc theo thể loại bài viết:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "Tất cả thể loại" chip
                            ElevatedFilterChip(
                                selected = selectedCategoryFilter == null,
                                onClick = { viewModel.setFilterCategory(null) },
                                label = { Text("Tất cả thể loại") },
                                modifier = Modifier.testTag("category_filter_all")
                            )

                            allCategories.forEach { category ->
                                val isSelected = selectedCategoryFilter == category
                                val count = categoryCounts[category] ?: 0
                                ElevatedFilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.setFilterCategory(if (isSelected) null else category)
                                    },
                                    label = {
                                        Text(if (count > 0) "$category ($count)" else category)
                                    },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier.testTag("category_filter_$category")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. FILTER THEO TÒA SOẠN BÁO
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Newspaper,
                                contentDescription = "Tòa soạn báo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lọc theo báo đưa tin:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ElevatedFilterChip(
                                selected = selectedNewspaperFilter == null,
                                onClick = { viewModel.setFilterNewspaper(null) },
                                label = { Text("Tất cả báo (${allTopicArticles.size})") }
                            )

                            newspaperCounts.forEach { (newspaper, count) ->
                                val isSelected = selectedNewspaperFilter == newspaper
                                val brandColor = getNewspaperBrandColor(newspaper)
                                ElevatedFilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setFilterNewspaper(newspaper) },
                                    label = { Text("$newspaper ($count)") },
                                    colors = FilterChipDefaults.elevatedFilterChipColors(
                                        selectedContainerColor = brandColor.copy(alpha = 0.2f),
                                        selectedLabelColor = brandColor
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Articles List
        if (!isScanning) {
            if (articles.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp, bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterAltOff,
                            contentDescription = "Trống",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Không tìm thấy bài viết thỏa mãn bộ lọc",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Thử điều chỉnh lại ngày đăng, thể loại hoặc bấm 'Xóa lọc' để xem toàn bộ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasActiveFilters) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { viewModel.resetAllFilters() }) {
                                Text("Đặt lại tất cả bộ lọc")
                            }
                        }
                    }
                }
            } else {
                items(articles, key = { it.id.takeIf { id -> id != 0L } ?: it.link }) { article ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        ArticleCard(
                            article = article,
                            onOpenWebsite = onOpenWebsite,
                            onToggleBookmark = onToggleBookmark
                        )
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMillis = datePickerState.selectedDateMillis
                        if (selectedMillis != null) {
                            viewModel.setCustomDate(selectedMillis)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        text = "Chọn ngày tháng năm xuất bản bài báo",
                        modifier = Modifier.padding(start = 24.dp, top = 16.dp),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            )
        }
    }
}

private fun exportAllArticlesToClipboard(
    context: Context,
    topic: String,
    articles: List<NewsArticle>
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val report = buildString {
        appendLine("==================================================")
        appendLine("TỔNG HỢP CÁC BÀI BÁO ĐƯA TIN VỀ: \"$topic\"")
        appendLine("Tổng số bài viết: ${articles.size} | Số tòa soạn: ${articles.map { it.newspaper }.distinct().size}")
        appendLine("Thời gian lập báo cáo: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(Date())}")
        appendLine("==================================================")
        appendLine()

        articles.forEachIndexed { index, art ->
            appendLine("Bài ${index + 1}:")
            appendLine("• Tiêu đề: ${art.title}")
            appendLine("• Thể loại: ${art.perspectiveTag}")
            appendLine("• Mô tả: ${art.description}")
            appendLine("• Ngày đăng: ${art.pubDate}")
            appendLine("• Báo đăng: ${art.newspaper} (${art.sourceUrl})")
            appendLine("• Link gốc: ${art.link}")
            appendLine("--------------------------------------------------")
        }
    }

    val clip = ClipData.newPlainText("Tổng hợp báo chí: $topic", report)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Đã xuất toàn bộ ${articles.size} bài viết vào bộ nhớ tạm", Toast.LENGTH_LONG).show()
}
