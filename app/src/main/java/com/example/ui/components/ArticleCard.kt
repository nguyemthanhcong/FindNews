package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NewsArticle

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ArticleCard(
    article: NewsArticle,
    onOpenWebsite: (NewsArticle) -> Unit,
    onToggleBookmark: (NewsArticle) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenWebsite(article) }
            .testTag("article_card_${article.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Source Newspaper and Metadata Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    NewspaperBadge(newspaper = article.newspaper)
                    if (article.perspectiveTag.isNotBlank() && article.perspectiveTag != "Tổng hợp") {
                        PerspectiveChip(tag = article.perspectiveTag)
                    }
                }

                IconButton(
                    onClick = { onToggleBookmark(article) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("bookmark_button_${article.id}")
                ) {
                    Icon(
                        imageVector = if (article.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (article.isBookmarked) "Bỏ lưu bài viết" else "Lưu bài viết",
                        tint = if (article.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 22.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description / Snippet
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Date & Outlet info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Ngày đăng",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = article.pubDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (article.sourceUrl.isNotBlank()) {
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = article.sourceUrl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action row: Website Tool button & Utility actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalButton(
                    onClick = { onOpenWebsite(article) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("open_website_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInBrowser,
                        contentDescription = "Mở website",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Xem trên ${article.newspaper}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Copy Formatted Citation
                    IconButton(
                        onClick = {
                            copyArticleCitation(context, article)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Sao chép tổng hợp",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            shareArticle(context, article)
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }
        }
    }
}

fun copyArticleCitation(context: Context, article: NewsArticle) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val formattedText = buildString {
        appendLine("📰 TIÊU ĐỀ: ${article.title}")
        appendLine("📝 MÔ TẢ: ${article.description}")
        appendLine("📅 NGÀY ĐĂNG: ${article.pubDate}")
        appendLine("🏛️ BÁO ĐĂNG: ${article.newspaper}")
        appendLine("🔗 LINK BÀI VIẾT: ${article.link}")
    }
    val clip = ClipData.newPlainText("Trích dẫn bài báo", formattedText)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Đã sao chép tổng hợp bài viết vào bộ nhớ tạm", Toast.LENGTH_SHORT).show()
}

fun shareArticle(context: Context, article: NewsArticle) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, article.title)
        putExtra(
            Intent.EXTRA_TEXT,
            "${article.title}\nBáo: ${article.newspaper} (${article.pubDate})\n${article.description}\n\nĐọc tại: ${article.link}"
        )
    }
    context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ bài báo"))
}
