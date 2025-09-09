package com.example.indra.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- Profile / Account ---
        item {
            SettingsOption(
                title = "Profile",
                subtitle = "Manage your account information",
                icon = Icons.Default.Person
            )
        }

        // --- Notifications ---
        item {
            SettingsOption(
                title = "Notifications",
                subtitle = "Alerts for water quality, maintenance, and updates",
                icon = Icons.Default.Notifications
            )
        }

        // --- Location Settings ---
        item {
            SettingsOption(
                title = "Location",
                subtitle = "Set your city/state for personalized water resources",
                icon = Icons.Default.LocationOn
            )
        }

        // --- Learning Hub ---
        item {
            SettingsOption(
                title = "Learning Hub",
                subtitle = "Customize article recommendations",
                icon = Icons.Default.MenuBook
            )
        }

        // --- Budget & Insurance ---
        item {
            SettingsOption(
                title = "Budget Preferences",
                subtitle = "Set budget alerts for installation/maintenance",
                icon = Icons.Default.AttachMoney
            )
        }

        // --- App Settings ---
        item {
            SettingsOption(
                title = "Theme",
                subtitle = "Light / Dark mode",
                icon = Icons.Default.DarkMode
            )
        }

        item {
            SettingsOption(
                title = "About",
                subtitle = "Version 1.0 • Prototype",
                icon = Icons.Default.Info
            )
        }
    }
}

@Composable
fun SettingsOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
