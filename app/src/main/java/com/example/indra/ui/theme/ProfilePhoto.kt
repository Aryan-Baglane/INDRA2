package com.example.indra.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.indra.R

@Composable
fun ProfilePhoto(photoUrl: String?, modifier: Modifier = Modifier) {
    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profile Photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        // fallback placeholder image
        Image(
            painter = rememberAsyncImagePainter(R.drawable.profile),
            contentDescription = "Default Profile Photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
