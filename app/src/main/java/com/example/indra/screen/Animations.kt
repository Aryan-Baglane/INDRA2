package com.example.indra.screen



import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import com.example.indra.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.coroutineScope

import kotlinx.coroutines.launch


@Composable
fun WaterDropExplodeAnimation(onFinished: () -> Unit) {
    val scale = remember { Animatable(1f) }
    val alpha = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Run scale and alpha animations with overlap for smoother feel
        coroutineScope {
            val scaleJob = launch {
                scale.animateTo(7.5f, animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing))
            }
            val alphaJob = launch {
                delay(200)
                alpha.animateTo(0f, animationSpec = tween(durationMillis = 700, easing = LinearOutSlowInEasing))
            }
            scaleJob.join()
            alphaJob.join()
        }
        onFinished()
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.hand_drawn_water_drop_cartoon_illustration),
            contentDescription = null,
            modifier = Modifier.scale(scale.value).alpha(alpha.value)
        )
    }
}


