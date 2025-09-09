package com.example.indra.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PostsContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(getSamplePosts()) { post ->
            PostCard(post = post)
        }
    }
}

@Composable
fun StoriesContent() {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Stories row
        LazyRow(
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getSampleStories()) { story ->
                StoryItem(story = story)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Posts with stories
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(getSamplePosts()) { post ->
                PostCard(post = post)
            }
        }
    }
}

@Composable
fun TipsContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(getSampleTips()) { tip ->
            TipCard(tip = tip)
        }
    }
}

@Composable
fun PostCard(post: CommunityPost) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // User info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.userName.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = post.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.timeAgo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                IconButton(onClick = { /* More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge
            )
            
            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                // Placeholder for image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "Image",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Water Conservation Image",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Engagement stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Likes",
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.likes} likes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Text(
                    text = "${post.comments} comments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.FavoriteBorder,
                    text = "Like",
                    onClick = { /* Like action */ }
                )
                ActionButton(
                    icon = Icons.Default.ChatBubbleOutline,
                    text = "Comment",
                    onClick = { /* Comment action */ }
                )
                ActionButton(
                    icon = Icons.Default.Share,
                    text = "Share",
                    onClick = { /* Share action */ }
                )
            }
        }
    }
}

@Composable
fun StoryItem(story: CommunityStory) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Story circle
        Box(
            modifier = Modifier
                .size(70.dp)
                .clip(CircleShape)
                .background(
                    if (story.isViewed) MaterialTheme.colorScheme.surfaceVariant 
                    else MaterialTheme.colorScheme.primary
                )
                .clickable { /* View story */ },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.userName.first().toString(),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = story.userName,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1
        )
    }
}

@Composable
fun TipCard(tip: CommunityTip) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Lightbulb,
                contentDescription = "Tip",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tip.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = tip.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Data classes
data class CommunityPost(
    val id: String,
    val userName: String,
    val content: String,
    val imageUrl: String? = null,
    val likes: Int,
    val comments: Int,
    val timeAgo: String
)

data class CommunityStory(
    val id: String,
    val userName: String,
    val isViewed: Boolean = false
)

data class CommunityTip(
    val id: String,
    val title: String,
    val description: String
)

// Sample data
fun getSamplePosts(): List<CommunityPost> = listOf(
    CommunityPost(
        id = "1",
        userName = "Water Warrior",
        content = "Just installed a rainwater harvesting system at my home! The feasibility assessment showed 85% potential. Excited to contribute to water conservation! 💧",
        imageUrl = "sample1.jpg",
        likes = 24,
        comments = 8,
        timeAgo = "2h ago"
    ),
    CommunityPost(
        id = "2",
        userName = "Eco Enthusiast",
        content = "Sharing my experience with recharge pits. They're perfect for areas with good soil permeability. Saved 50,000 liters this monsoon! 🌧️",
        likes = 18,
        comments = 5,
        timeAgo = "4h ago"
    ),
    CommunityPost(
        id = "3",
        userName = "Green Living",
        content = "Community workshop on water conservation techniques was amazing! Learned so much about sustainable water management. #WaterConservation",
        imageUrl = "sample3.jpg",
        likes = 32,
        comments = 12,
        timeAgo = "6h ago"
    )
)

fun getSampleStories(): List<CommunityStory> = listOf(
    CommunityStory("1", "Water Warrior", false),
    CommunityStory("2", "Eco Enthusiast", true),
    CommunityStory("3", "Green Living", false),
    CommunityStory("4", "Rain Collector", true),
    CommunityStory("5", "Sustainable Life", false)
)

fun getSampleTips(): List<CommunityTip> = listOf(
    CommunityTip(
        id = "1",
        title = "Roof Area Calculation",
        description = "Measure your roof area accurately. Multiply length by width for rectangular roofs, or use online calculators for complex shapes."
    ),
    CommunityTip(
        id = "2",
        title = "Storage Tank Sizing",
        description = "Size your storage tank based on 20-30 days of water requirement. Consider family size and daily consumption patterns."
    ),
    CommunityTip(
        id = "3",
        title = "Maintenance Schedule",
        description = "Clean gutters monthly, inspect storage tanks quarterly, and service pumps annually for optimal performance."
    )
)
