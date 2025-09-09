package com.example.indra.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.indra.data.Article

// --- Category color helper ---
@Composable
fun getCategoryColor(category: String): Color {
    return when (category) {
        "Aquifers" -> Color(0xFFB2DFDB) // Teal
        "DIY" -> Color(0xFFF0F4C3) // Lime
        "Stories" -> Color(0xFFBBDEFB) // Light Blue
        "Policies" -> Color(0xFFFFCCBC) // Orange
        "Maintenance" -> Color(0xFFD1C4E9) // Deep Purple
        "Health" -> Color(0xFFC5E1A5) // Green
        "Community" -> Color(0xFFF8BBD0) // Pink
        "Technology" -> Color(0xFFCFD8DC) // Blue-grey
        "Gardening" -> Color(0xFFE6EE9C) // Light Lime
        "Budgeting" -> Color(0xFFBCAAA4) // Brown
        else -> MaterialTheme.colorScheme.surface
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnHubView() {
    val articles = listOf(
        Article("Types of Aquifers", "Understand the underground layers storing water.", "Aquifers"),
        Article("DIY Filter Guide", "Learn to build a simple filter for your RWH system.", "DIY"),
        Article("Success Stories from Your State", "Get inspired by local conservation heroes.", "Stories"),
        Article("Government Subsidies & Policies", "Find out how the government can help you.", "Policies"),
        Article("Maintaining Your RWH System", "Tips for ensuring your system runs efficiently.", "Maintenance"),
        Article("Impact of Water Quality on Health", "Explore the link between clean water and well-being.", "Health"),
        Article("Starting a Community Water Project", "A step-by-step guide to collaborative initiatives.", "Community"),
        Article("Smart RWH: Using IoT for Water Management", "Discover how technology can optimize your system.", "Technology"),
        Article("Water-Wise Gardening Techniques", "Methods to keep your garden lush while saving water.", "Gardening"),
        Article("Budgeting for Your RWH System", "Tips to plan and manage the finances of installation.", "Budgeting")
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Learn & Grow",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(articles) { article ->
            ExpandableArticleCard(article = article)
        }
    }
}

@Composable
fun ExpandableArticleCard(article: Article) {
    var expanded by remember { mutableStateOf(false) }
    val cardColor = getCategoryColor(article.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = spring())
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor) // Card colored
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Tag chip (light background for contrast)
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = article.category,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Title
            Text(
                text = article.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Summary (always visible)
            Text(
                text = article.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Expanded details
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Here goes the full details of the article. " +
                                "You can replace this with actual fetched/longer content. " +
                                "Expanding smoothly shows this section.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Chevron icon (aligned right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .rotate(if (expanded) 90f else 0f)
                )
            }
        }
    }
}