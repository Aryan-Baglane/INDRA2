package com.example.indra.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.indra.auth.AuthApi
import com.example.indra.data.AuthUser
import com.example.indra.data.UserProfile
import com.example.indra.db.DatabaseProvider
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onSignedOut: () -> Unit = {}) {
    var user by remember { mutableStateOf<AuthUser?>(null) }
    var loading by remember { mutableStateOf(true) }
    var editing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        user = AuthApi.currentUser()
        loading = false
        name = user?.displayName.orEmpty()
        user?.uid?.let { uid ->
            val existing = DatabaseProvider.database().getUserProfile(uid)
            if (existing != null) {
                name = existing.displayName ?: name
            }
            val authPhoto = user?.photoUrl
            if (!authPhoto.isNullOrBlank() && (existing == null || existing.photoUrl != authPhoto)) {
                DatabaseProvider.database().setUserProfile(
                    UserProfile(
                        uid = uid,
                        displayName = name.ifBlank { user?.displayName },
                        photoUrl = authPhoto
                    )
                )
            }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (user == null) {
        AuthScreen(
            onSignedIn = {
                editing = false
                loading = true
                scope.launch {
                    user = AuthApi.currentUser()
                    loading = false
                    name = user?.displayName.orEmpty()
                }
            },
            onGoogleSignIn = {}
        )
    } else {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Color(0xFFE3F2FD) // Light blue background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!editing) {
                    // Profile Header Card
                    ProfileHeader(user = user, onEditClick = { editing = true })

                    Spacer(modifier = Modifier.height(24.dp))

                    // Menu Options
                    OptionCard(
                        icon = Icons.Default.HomeWork,
                        title = "My Properties",
                        description = "Manage your saved properties"
                    ) {
                        // TODO: Implement navigation to My Properties screen
                    }

                    OptionCard(
                        icon = Icons.Default.Lock,
                        title = "Change Password",
                        description = "Update your account password"
                    ) {
                        // TODO: Implement navigation to Change Password screen
                    }

                    OptionCard(
                        icon = Icons.Default.Settings,
                        title = "Settings",
                        description = "Configure app settings"
                    ) {
                        // TODO: Implement navigation to Settings screen
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Sign Out Button
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                AuthApi.signOut()
                                user = null
                                onSignedOut()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            width = 2.dp,

                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Logout, contentDescription = "Sign Out")
                            Spacer(Modifier.width(8.dp))
                            Text("Sign Out", fontSize = 16.sp)
                        }
                    }
                } else {
                    // Edit Profile View
                    EditProfileView(
                        name = name,
                        onNameChange = { name = it },
                        onSave = {
                            scope.launch {
                                AuthApi.updateProfile(displayName = name, photoUrl = null)
                                user = AuthApi.currentUser()
                                user?.uid?.let { uid ->
                                    DatabaseProvider.database().setUserProfile(
                                        UserProfile(uid = uid, displayName = name)
                                    )
                                }
                                editing = false
                                snackbarHostState.showSnackbar("Profile saved!")
                            }
                        },
                        onCancel = { editing = false }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(user: AuthUser?, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = user?.photoUrl),
                contentDescription = "Profile Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = user?.displayName ?: "User Name",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = user?.email ?: "user@example.com",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEditClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Edit Profile")
            }
        }
    }
}

@Composable
fun OptionCard(icon: ImageVector, title: String, description: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun EditProfileView(
    name: String,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Edit Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Display name") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
            }
        }
    }
}

// NOTE: Add `coil-compose` dependency to your `build.gradle` file for `rememberAsyncImagePainter` to work:
// implementation("io.coil-kt:coil-compose:2.5.0")