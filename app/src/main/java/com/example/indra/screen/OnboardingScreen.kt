package com.example.indra.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.UserProfile
import com.example.indra.db.DatabaseProvider
import com.example.indra.auth.AuthApi
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onCompleted: () -> Unit) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var numDwellers by remember { mutableStateOf("3") }
    var roofArea by remember { mutableStateOf("60") }
    var openSpace by remember { mutableStateOf("20") }
    var roofType by remember { mutableStateOf("Concrete") }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Prefill from profile if exists
    LaunchedEffect(Unit) {
        try {
            val user = AuthApi.currentUser()
            if (user != null) {
                val profile = DatabaseProvider.database().getUserProfile(user.uid)
                if (profile != null) {
                    name = profile.name.ifBlank { profile.displayName ?: "" }
                    if (profile.numDwellers > 0) numDwellers = profile.numDwellers.toString()
                    if (profile.roofAreaSqm > 0) roofArea = profile.roofAreaSqm.toString()
                    if (profile.openSpaceSqm > 0) openSpace = profile.openSpaceSqm.toString()
                    roofType = profile.roofType
                } else {
                    name = user.displayName ?: ""
                }
            }
        } catch (_: Exception) {}
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome! Let's personalize your dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = numDwellers,
                onValueChange = { numDwellers = it.filter { ch -> ch.isDigit() } },
                label = { Text("Number of dwellers") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = roofArea,
                onValueChange = { roofArea = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Roof area (sqm)") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = openSpace,
                onValueChange = { openSpace = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Open space (sqm)") },
                singleLine = true
            )
            Spacer(Modifier.height(12.dp))

            // Simple roof type chooser
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Roof type:")
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = roofType == "Concrete", onClick = { roofType = "Concrete" }, label = { Text("Concrete") })
                Spacer(Modifier.width(8.dp))
                FilterChip(selected = roofType == "Tiled", onClick = { roofType = "Tiled" }, label = { Text("Tiled") })
            }

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(20.dp))
            Button(enabled = !isSaving, onClick = {
                isSaving = true
                error = null
                scope.launch {
                    try {
                        val user = AuthApi.currentUser() ?: throw IllegalStateException("Not signed in")
                        val profile = UserProfile(
                            uid = user.uid,
                            displayName = user.displayName,
                            photoUrl = user.photoUrl,
                            name = name,
                            onboardingCompleted = true,
                            numDwellers = numDwellers.toIntOrNull() ?: 0,
                            roofAreaSqm = roofArea.toDoubleOrNull() ?: 0.0,
                            openSpaceSqm = openSpace.toDoubleOrNull() ?: 0.0,
                            roofType = roofType
                        )
                        DatabaseProvider.database().setUserProfile(profile)
                        onCompleted()
                    } catch (e: Exception) {
                        error = e.message
                    } finally {
                        isSaving = false
                    }
                }
            }) {
                Text("Continue")
            }
        }
    }
}


