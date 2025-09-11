package com.example.indra.screen

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.indra.R
import com.google.android.gms.location.*
import kotlinx.coroutines.delay
import java.util.*
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.launch
import com.example.indra.R.drawable.hand_drawn_water_drop_cartoon_illustration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onStartAssessment: () -> Unit) {
    var currentLocation by remember { mutableStateOf("Fetching location...") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }
    var feasibilityScore by remember { mutableStateOf<Double?>(null) }
    var potentialRunoff by remember { mutableStateOf<Double?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val scope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                fetchLocationFull(context, fusedClient) { addr, lat, lon ->
                    currentLocation = addr ?: "Unable to fetch location"
                    latitude = lat
                    longitude = lon
                }
            } else {
                currentLocation = "Permission denied"
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

    LaunchedEffect(latitude, longitude) {
        val lat = latitude
        val lon = longitude
        if (lat != null && lon != null) {
            isLoading = true
            error = null
            scope.launch {
                try {
                    // Fetch onboarding profile
                    val user = com.example.indra.auth.AuthApi.currentUser()
                    val uid = user?.uid
                    if (uid != null) {
                        val profile = com.example.indra.db.DatabaseProvider.database().getUserProfile(uid)
                        if (profile != null && profile.onboardingCompleted) {
                            val request = com.example.indra.data.AssessmentRequest(
                                name = profile.displayName ?: "Home",
                                latitude = lat,
                                longitude = lon,
                                numDwellers = profile.numDwellers,
                                roofAreaSqm = profile.roofAreaSqm,
                                openSpaceSqm = profile.openSpaceSqm,
                                roofType = profile.roofType
                            )
                            val repo = com.example.indra.data.AssessmentRepositoryProvider.repository()
                            val result = repo.performAssessment(request)
                            result.onSuccess { resp ->
                                feasibilityScore = resp.feasibilityScore
                                potentialRunoff = resp.rwhAnalysis.potentialAnnualRunoffLiters
                            }.onFailure { e -> error = e.message }
                        }
                    }
                } catch (e: Exception) {
                    error = e.message
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFE3F2FD), Color.White)
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedEntrance {
                Text(
                    "Personalised Feasibility Report",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black.copy(alpha = 0.7f),
                    fontSize = 26.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            AnimatedEntrance(delayMillis = 120) { Metrics(currentLocation = currentLocation, score = feasibilityScore, loading = isLoading) }
            AnimatedEntrance(delayMillis = 240) { HarvestingPotential(potentialRunoff) }
            AnimatedEntrance(delayMillis = 360) { Recommandation() }
            AnimatedEntrance(delayMillis = 480) { StartNewAssessmentCard(onClick = onStartAssessment) }
        }
    }
}

// Entrance animation for all dashboard sections
@Composable
fun AnimatedEntrance(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, animationSpec = tween(700), label = "fadeIn")
    val translateY by animateFloatAsState(if (visible) 0f else 40f, animationSpec = tween(800), label = "slideUp")

    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }

    Box(
        modifier = Modifier
            .graphicsLayer { this.alpha = alpha; translationY = translateY }
            .fillMaxWidth()
    ) {
        content()
    }
}

@Composable
fun Metrics(currentLocation: String = "Current Location", score: Double? = null, loading: Boolean = false) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationChip(currentLocation)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Feasibility Score",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(10.dp))
            ProgressAnimated(target = if (score != null) (score / 100.0).toFloat().coerceIn(0f,1f) else null, loading = loading)
        }
    }
}

@Composable
fun LocationChip(location: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFB3E5FC).copy(alpha = 0.7f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Home, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
            Text(location, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun ProgressAnimated(target: Float? = null, loading: Boolean = false) {
    var progressTarget by remember { mutableStateOf(0f) }
    LaunchedEffect(target) { progressTarget = target ?: if (loading) 0f else 0.0f }
    val animatedProgress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(1500, easing = LinearOutSlowInEasing),
        label = "progressAnim"
    )
    val animatedPercentage = (animatedProgress * 100).toInt()

    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.size(120.dp),
            color = Color(0xFF0288D1),
            strokeWidth = 8.dp,
            trackColor = Color(0xFFB3E5FC).copy(alpha = 0.7f),
        )
        Text(
            "${if (target == null && loading) "..." else "$animatedPercentage%"}",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF0288D1),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(6.dp)
        )
    }
}

@Composable
fun HarvestingPotential(potentialRunoffLiters: Double? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painterResource(R.drawable.rain_11198966),
                contentDescription = "Rain",
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Annual Harvesting Potential",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = potentialRunoffLiters?.let {
                        val v = it.toLong()
                        "${String.format(Locale.getDefault(), "%,d", v)} liters"
                    } ?: "Calculating...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0288D1)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Image(
                painter = painterResource(R.drawable.stats_11072737),
                contentDescription = "Stats",
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
fun Recommandation() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .padding(vertical = 10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.rain_water_12277899),
                contentDescription = "Recommendation",
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Recommended Solution", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recharge Pit", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black.copy(alpha = 0.65f))
                    Image(
                        painter = painterResource(R.drawable.save_water),
                        contentDescription = "Water Save",
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StartNewAssessmentCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(98.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Start New Assessment",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Tap to estimate harvesting feasibility",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Filled.People,
                contentDescription = "Assessment Icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

// Location helper functions
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

@SuppressLint("MissingPermission")
private fun fetchLocationFull(
    context: Context,
    fusedClient: FusedLocationProviderClient,
    callback: (String?, Double?, Double?) -> Unit
) {
    fusedClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            if (location != null) {
                callback(location.toAddress(context), location.latitude, location.longitude)
            } else {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000
                ).setMaxUpdates(1).build()

                fusedClient.requestLocationUpdates(
                    request,
                    object : LocationCallback() {
                        override fun onLocationResult(result: LocationResult) {
                            val loc = result.lastLocation
                            callback(loc?.toAddress(context), loc?.latitude, loc?.longitude)
                            fusedClient.removeLocationUpdates(this)
                        }
                    },
                    null
                )
            }
        }
        .addOnFailureListener {
            callback(null, null, null)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(onMenuClick: () -> Unit = {}) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
        title = { Text("Jal Sanchay Mitra", style = MaterialTheme.typography.titleLarge, fontSize = 22.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            Image(
                painter = painterResource(id = R.drawable.hand_drawn_water_drop_cartoon_illustration),
                contentDescription = "Water Drop Logo",
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(48.dp)
            )
        },
        actions = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    )
}