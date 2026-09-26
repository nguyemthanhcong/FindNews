package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Article
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ColorDanTri
import com.example.ui.theme.ColorDefaultNews
import com.example.ui.theme.ColorLaoDong
import com.example.ui.theme.ColorThanhNien
import com.example.ui.theme.ColorTienPhong
import com.example.ui.theme.ColorTuoiTre
import com.example.ui.theme.ColorVTV
import com.example.ui.theme.ColorVietnamNet
import com.example.ui.theme.ColorVnExpress

fun getNewspaperBrandColor(newspaper: String): Color {
    val lower = newspaper.lowercase()
    return when {
        lower.contains("tuổi trẻ") -> ColorTuoiTre
        lower.contains("vnexpress") -> ColorVnExpress
        lower.contains("dân trí") -> ColorDanTri
        lower.contains("thanh niên") -> ColorThanhNien
        lower.contains("lao động") -> ColorLaoDong
        lower.contains("vietnamnet") -> ColorVietnamNet
        lower.contains("tiền phong") -> ColorTienPhong
        lower.contains("vtv") -> ColorVTV
        lower.contains("sức khỏe") -> Color(0xFF0D9488) // Teal
        lower.contains("công an") || lower.contains("quân đội") -> Color(0xFF991B1B)
        lower.contains("nhân dân") -> Color(0xFFDC2626)
        lower.contains("pháp luật") -> Color(0xFF4338CA)
        else -> ColorDefaultNews
    }
}

@Composable
fun NewspaperBadge(
    newspaper: String,
    modifier: Modifier = Modifier
) {
    val brandColor = getNewspaperBrandColor(newspaper)

    Surface(
        color = brandColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(brandColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = newspaper.ifBlank { "Báo điện tử" },
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                ),
                color = brandColor
            )
        }
    }
}

@Composable
fun PerspectiveChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (tag) {
        "Y tế & Sức khỏe" -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        "Chính sách & Pháp lý" -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        "Điều tra & Xử lý" -> Pair(Color(0xFFFEE2E2), Color(0xFFB91C1C))
        "Thị trường & Kinh tế" -> Pair(Color(0xFFDCFCE7), Color(0xFF15803D))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Text(
            text = tag,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.5.sp
            ),
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
        )
    }
}
