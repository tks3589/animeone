package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign


@Composable
fun SpoilerText(
    text: String,
    modifier: Modifier = Modifier
) {
    var revealed by rememberSaveable(text) { mutableStateOf(false) }

    val displayText = if (revealed) {
        text
    } else {
        "▇".repeat(text.length)
    }

    val backgroundColor = if (revealed) {
        Color.Transparent
    } else {
        MaterialTheme.colorScheme.outline
    }

    CommonTextS(
        text = displayText,
        modifier = modifier
            .clickable { revealed = true }
            .background(backgroundColor),
        textAlign = TextAlign.Start
    )
}