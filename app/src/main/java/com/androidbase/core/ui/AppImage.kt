package com.androidbase.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable fun AppImage(url: String?, description: String?, modifier: Modifier = Modifier) {
    AsyncImage(model = url, contentDescription = description, modifier = modifier, contentScale = ContentScale.Crop)
}
