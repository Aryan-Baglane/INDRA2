package com.example.indra.screen





import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.indra.R.drawable.hand_drawn_water_drop_cartoon_illustration
import com.example.indra.auth.AuthApi
import com.example.indra.platform.PlatformSignIn
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    onGoogleSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // ✅ Launcher for Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            PlatformSignIn.handleResult(result.data)
        } else {
            error = "Google sign-in canceled"
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(hand_drawn_water_drop_cartoon_illustration),
                    contentDescription = null
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Jal Snchay Mitra",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                if (isSignUp) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Name (optional)") }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") })
                Spacer(Modifier.height(16.dp))

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        enabled = !isLoading,
                        onClick = {
                            isLoading = true
                            error = null
                            scope.launch {
                                val result = if (isSignUp) {
                                    AuthApi.signUpWithEmail(
                                        email,
                                        password,
                                        displayName.takeIf { it.isNotBlank() }
                                    )
                                } else {
                                    AuthApi.signInWithEmail(email, password)
                                }
                                isLoading = false
                                result
                                    .onSuccess { onSignedIn() }
                                    .onFailure { error = it.message }
                            }
                        }
                    ) {
                        Text(if (isSignUp) "Sign up" else "Sign in")
                    }

                    // ✅ Corrected Google sign-in button
                    OutlinedButton(
                        enabled = !isLoading,
                        onClick = {
                            isLoading = true
                            error = null
                            PlatformSignIn.setCallback { success, errorMsg ->
                                isLoading = false
                                if (success) onSignedIn() else error = errorMsg
                            }
                            PlatformSignIn.signIn(launcher)
                        }
                    ) {
                        Text("Continue with Google")
                    }
                }

                TextButton(onClick = { isSignUp = !isSignUp }) {
                    Text(if (isSignUp) "Have an account? Sign in" else "New here? Sign up")
                }
            }
        }
    }
}
