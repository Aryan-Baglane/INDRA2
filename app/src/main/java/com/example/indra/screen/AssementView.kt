package com.example.indra.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.data.Report
import com.example.indra.data.ReportRepositoryProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentView(
    onSubmit: (name: String, location: String, dwellers: Int, roofArea: Double, openSpace: Double) -> Unit = { _, _, _, _, _ -> },
    onReportGenerated: (Report) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dwellers by remember { mutableStateOf("") }
    var roofArea by remember { mutableStateOf("") }
    var openSpace by remember { mutableStateOf("") }
    var userLocation by remember { mutableStateOf("Fetching location...") }
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                fetchLocation(context, fusedClient) { addr ->
                    userLocation = addr ?: "Unable to fetch location"
                }
            } else {
                userLocation = "Permission denied"
            }
        }
    )

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFE3F2FD), Color.White)
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "New Assessment",
                style = MaterialTheme.typography.headlineLarge,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = 0.85f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Enter your property details to generate a personalised report.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 20.dp, shape = RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name of Property") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = userLocation,
                        onValueChange = { userLocation = it },
                        label = { Text("Location") },
                        singleLine = false,
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = dwellers,
                        onValueChange = { if (it.length <= 3) dwellers = it },
                        label = { Text("Number of Dwellers") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = roofArea,
                        onValueChange = { roofArea = it },
                        label = { Text("Roof Area (sq. m)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = openSpace,
                        onValueChange = { openSpace = it },
                        label = { Text("Open Space (sq. m)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {
                    val dwellersInt = dwellers.toIntOrNull() ?: 0
                    val roofAreaDouble = roofArea.toDoubleOrNull() ?: 0.0
                    val openSpaceDouble = openSpace.toDoubleOrNull() ?: 0.0

                    scope.launch {
                        val generatedReport = Report(
                            id = UUID.randomUUID().toString(),
                            name = name,
                            location = userLocation,
                            dwellers = dwellersInt,
                            roofArea = roofAreaDouble,
                            openSpace = openSpaceDouble,
                            feasibilityScore = 85.0f,
                            annualHarvestingPotentialLiters = 15000,
                            recommendedSolution = "Rooftop rainwater harvesting system",
                            estimatedCostInr = 55000,
                            timestamp = System.currentTimeMillis()
                        )

                        val repository = ReportRepositoryProvider.repository()
                        val isSaved = repository.addReport(generatedReport)

                        if (isSaved) {
                            snackbarHostState.showSnackbar(
                                message = "Report generated and saved successfully!",
                                duration = SnackbarDuration.Short
                            )
                            onReportGenerated(generatedReport)
                        } else {
                            snackbarHostState.showSnackbar(
                                message = "Report generated but failed to save. Please try again.",
                                duration = SnackbarDuration.Long
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    "Generate Report",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

// ... (fetchLocation and Location.toAddress functions remain the same)
@SuppressLint("MissingPermission")
private fun fetchLocation(
    context: Context,
    fusedClient: FusedLocationProviderClient,
    callback: (String?) -> Unit
) {
    fusedClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location.toAddress(context))
            } else {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).setMaxUpdates(1).build()

                fusedClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val loc = result.lastLocation
                            callback(loc?.toAddress(context))
                            fusedClient.removeLocationUpdates(this)
                        }
                    },
                    null
                )
            }
        }
        .addOnFailureListener {
            callback(null)
        }
}

private fun Location.toAddress(context: Context): String? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (!addresses.isNullOrEmpty()) {
            val addr = addresses[0]
            "${addr.featureName}, ${addr.locality}, ${addr.adminArea}, ${addr.countryName}"
        } else null
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}