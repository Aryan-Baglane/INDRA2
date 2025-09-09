package com.example.indra.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.example.indra.R
import com.example.indra.data.RainwaterStats
import kotlinx.coroutines.delay
import kotlin.random.Random

@Preview
@Composable
fun MyHouseScreen(modifier: Modifier = Modifier) {
    var stats by remember { mutableStateOf(RainwaterStats()) }

    // Fake simulation for rain filling/draining
    LaunchedEffect(stats.isRaining) {
        while (true) {
            delay(1000)
            stats = if (stats.isRaining) {
                stats.copy(
                    waterCollectedToday = stats.waterCollectedToday + 0.1f,
                    tankLevel = (stats.tankLevel + 0.1f).coerceAtMost(100f),
                    undergroundTankLevel = (stats.undergroundTankLevel + 0.15f).coerceAtMost(100f),
                    currentRainfall = 5.5f
                )
            } else {
                stats.copy(
                    undergroundTankLevel = (stats.undergroundTankLevel - stats.consumptionRate).coerceAtLeast(0f),
                    tankLevel = (stats.tankLevel - stats.consumptionRate * 0.5f).coerceAtLeast(0f),
                    currentRainfall = 0f
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ---------------- CONSTRAINT LAYOUT ----------------
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (
                background, rooftopTank, undergroundTank,
                verticalPipe, horizontalPipe, longVerticalPipe
            ) = createRefs()

            // Background image
            Image(
                painter = painterResource(id = R.drawable.untitled),
                contentDescription = "House background",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .constrainAs(background) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )

            // Underground Tank
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(Color.DarkGray.copy(alpha = 0.6f))
                    .border(1.dp,Color.Black, shape = RoundedCornerShape(2.dp))
                    .constrainAs(undergroundTank) {
                        bottom.linkTo(background.bottom, margin = -120.dp)
                        start.linkTo(background.start, margin = 75.dp)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(stats.undergroundTankLevel / 100f)
                        .background(Color(0xFF4FC3F7))
                        .align(Alignment.BottomCenter)
                )
            }

            // Rooftop Tank
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.DarkGray.copy(alpha = 0.6f))
                    .border(1.dp,Color.Black, shape = RoundedCornerShape(2.dp))
                    .constrainAs(rooftopTank) {
                        top.linkTo(background.top, margin = 75.dp)
                        start.linkTo(background.start, margin = 275.dp)
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(stats.tankLevel / 100f)
                        .background(Color(0xFF4FC3F7))
                        .align(Alignment.BottomCenter)
                )
            }

            // Vertical Pipe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(80.dp)
                    .background(Color.Gray.copy(alpha = 0.8f))
                    .clipToBounds()
                    .constrainAs(verticalPipe) {
                        top.linkTo(rooftopTank.bottom, margin = 2.dp)
                        start.linkTo(rooftopTank.end, margin = -50.dp)
                    }
            ) {
                if (!stats.isRaining) FlowingWaterEffect(isHorizontal = false)
            }

            // Horizontal Pipe
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(6.dp)
                    .background(Color.Gray.copy(alpha = 0.8f))
                    .clipToBounds()
                    .constrainAs(horizontalPipe) {
                        top.linkTo(verticalPipe.bottom, margin = 0.dp)
                        start.linkTo(verticalPipe.start, margin = -40.dp)
                    }
            ) {
                if (!stats.isRaining) FlowingWaterEffect(isHorizontal = true, reverse = true)
            }

            // Long Vertical Pipe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(120.dp)
                    .background(Color.Gray.copy(alpha = 0.8f))
                    .clipToBounds()
                    .constrainAs(longVerticalPipe) {
                        top.linkTo(horizontalPipe.bottom,margin = -50.dp)
                        start.linkTo(horizontalPipe.start, margin = (-170).dp)
                    }
            ) {
                if (stats.isRaining) FlowingWaterEffect(isHorizontal = false)
            }
        }

        // ---------------- RAIN ANIMATION ----------------
        if (stats.isRaining) {
            RainAnimation()
        }

        // ---------------- STATS PANEL ----------------
        StatsPanel(modifier = Modifier.align(Alignment.BottomCenter), stats = stats)

        // ---------------- WEATHER CONTROL ----------------
        WeatherControls(
            modifier = Modifier.align(Alignment.BottomEnd),
            isRaining = stats.isRaining,
            onToggleRain = { stats = stats.copy(isRaining = !stats.isRaining) }
        )
    }
}

// ---------------- FLOWING WATER EFFECT ----------------
@Composable
fun BoxScope.FlowingWaterEffect(isHorizontal: Boolean, reverse: Boolean = false) {
    val infiniteTransition = rememberInfiniteTransition(label = "waterFlowTransition")
    val flowProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "flowProgress"
    )

    Box(
        modifier = Modifier.fillMaxSize().clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isHorizontal) Modifier.fillMaxHeight().fillMaxWidth(0.3f)
                    else Modifier.fillMaxWidth().fillMaxHeight(0.3f)
                )
                .align(
                    when {
                        isHorizontal && reverse -> Alignment.CenterEnd
                        isHorizontal -> Alignment.CenterStart
                        !isHorizontal && reverse -> Alignment.BottomCenter
                        else -> Alignment.TopCenter
                    }
                )
                .offset(
                    x = if (isHorizontal) ((if (reverse) 1f - flowProgress else flowProgress) * 100).dp else 0.dp,
                    y = if (!isHorizontal) ((if (reverse) 1f - flowProgress else flowProgress) * 100).dp else 0.dp
                )
                .background(Color(0xFF4FC3F7))
        )
    }
}

// ---------------- RAIN ANIMATION ----------------
@Composable
fun RainAnimation() {
    val raindrops = remember {
        List(90) {
            Raindrop(
                xOffset = Random.nextFloat(),
                delay = Random.nextInt(0, 1000),
                duration = Random.nextInt(300, 600),
                thickness = Random.nextInt(1, 4),
                length = Random.nextInt(12, 28)
            )
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "")

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        raindrops.forEach { drop ->
            val yPosition by infiniteTransition.animateFloat(
                initialValue = -0.1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = drop.duration, delayMillis = drop.delay),
                    repeatMode = RepeatMode.Restart
                ), label = ""
            )

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (drop.xOffset * widthPx).toInt(),
                            (yPosition * heightPx).toInt()
                        )
                    }
                    .width(drop.thickness.dp)
                    .height(drop.length.dp)
                    .background(Color(0xFF3B82F6).copy(alpha = 0.5f), RoundedCornerShape(50))
            )

            if (yPosition >= 0.95f) {
                val splashAlpha = 1f - ((yPosition - 0.95f) * 10f).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (drop.xOffset * widthPx).toInt(),
                                (0.95f * heightPx).toInt()
                            )
                        }
                        .size((10 + drop.thickness * 2).dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6).copy(alpha = splashAlpha))
                )
            }
        }
    }
}

private data class Raindrop(
    val xOffset: Float,
    val delay: Int,
    val duration: Int,
    val thickness: Int,
    val length: Int
)


// ---------------- WEATHER CONTROL ----------------
@Composable
fun WeatherControls(
    modifier: Modifier = Modifier,
    isRaining: Boolean,
    onToggleRain: () -> Unit
) {
    Box(modifier = modifier.padding(16.dp)) {
        FloatingActionButton(
            onClick = onToggleRain,
            shape = CircleShape,
            containerColor = Color.White.copy(alpha = 0.95f),
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                painter = painterResource(id = if (isRaining) R.drawable.cloud else R.drawable.wbsunny),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

// ---------------- STATS PANEL ----------------
@Composable
fun StatsPanel(modifier: Modifier = Modifier, stats: RainwaterStats) {
    AnimatedVisibility(visible = true, modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Rainwater System Stats", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(
                                if (stats.isRaining) Color(0xFF3B82F6) else Color.LightGray,
                                CircleShape
                            )
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        icon = painterResource(R.drawable.waterdrop),
                        label = if (stats.isRaining) "Rainfall Rate" else "Consumption Rate",
                        value = if (stats.isRaining)
                            "${"%.1f".format(stats.currentRainfall)} mm/hr"
                        else
                            "-${"%.2f".format(stats.consumptionRate)}%/s",
                        iconColor = if (stats.isRaining) Color(0xFF3B82F6) else Color(0xFFF87171)
                    )
                    StatItem(
                        icon = painterResource(R.drawable.trendingup),
                        label = "Collected Today",
                        value = "${"%.0f".format(stats.waterCollectedToday)} L",
                        iconColor = Color(0xFF22C55E)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatItem(
                        icon = painterResource(R.drawable.batteryfull),
                        label = "Rooftop Tank",
                        value = "${"%.0f".format(stats.tankLevel)}%",
                        iconColor = Color(0xFFF97316)
                    )
                    StatItem(
                        icon = painterResource(R.drawable.batteryfull),
                        label = "Underground Tank",
                        value = "${"%.0f".format(stats.undergroundTankLevel)}%",
                        iconColor = Color(0xFF0EA5E9)
                    )
                }

                Spacer(Modifier.height(8.dp))

                val animatedProgress by animateFloatAsState(
                    targetValue = stats.undergroundTankLevel / 100f,
                    animationSpec = tween(1000), label = ""
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                )

                Spacer(Modifier.height(16.dp))

                // NEW: Smarter status messages
                val status = when {
                    stats.isRaining -> "Status: Actively Collecting Water"
                    stats.undergroundTankLevel <= 10f -> "Status: Warning - Low Water!"
                    stats.undergroundTankLevel > 90f -> "Status: Tanks Nearly Full"
                    else -> "Status: System Ready - Supplying Water"
                }

                Text(
                    text = status,
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        stats.isRaining -> Color(0xFF3B82F6)
                        stats.undergroundTankLevel <= 10f -> Color(0xFFDC2626)
                        stats.undergroundTankLevel > 90f -> Color(0xFF22C55E)
                        else -> Color.Gray
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.StatItem(icon: Painter, label: String, value: String, iconColor: Color) {
    Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
    }
}