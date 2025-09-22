package com.aaron.chen.animeone.app.view.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

object FontSize {
    val xxxl = 32.dp
    val xxl = 24.dp
    val xl = 20.dp
    val l = 18.dp
    val m = 16.dp
    val s = 14.dp
    val xs = 12.dp
    val xxs = 10.dp
}

@Composable
fun dpTextUnit(dp: Dp): TextUnit = with(LocalDensity.current) { dp.toSp() }