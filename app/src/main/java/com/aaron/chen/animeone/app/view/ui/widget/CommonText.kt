package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.aaron.chen.animeone.app.view.ui.theme.FontFamilyBold
import com.aaron.chen.animeone.app.view.ui.theme.FontFamilyRegular
import com.aaron.chen.animeone.app.view.ui.theme.FontSize
import com.aaron.chen.animeone.app.view.ui.theme.dpTextUnit

@Composable
fun CommonTextM(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    CommonTextBase(
        text = text,
        fontSize = FontSize.m,
        modifier = modifier,
        bold = bold,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun CommonTextS(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    CommonTextBase(
        text = text,
        fontSize = FontSize.s,
        modifier = modifier,
        bold = bold,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun CommonTextL(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    CommonTextBase(
        text = text,
        fontSize = FontSize.l,
        modifier = modifier,
        bold = bold,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun CommonTextXS(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    CommonTextBase(
        text = text,
        fontSize = FontSize.xs,
        modifier = modifier,
        bold = bold,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun CommonTextXXL(
    text: String,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    CommonTextBase(
        text = text,
        fontSize = FontSize.xxl,
        modifier = modifier,
        bold = bold,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}

@Composable
fun CommonTextBase(
    text: String,
    fontSize: Dp,
    modifier: Modifier = Modifier,
    bold: Boolean = false,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = MaterialTheme.colorScheme.onBackground,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        fontSize = dpTextUnit(fontSize),
        modifier = modifier,
        fontFamily = if (bold) FontFamilyBold else FontFamilyRegular,
        textAlign = textAlign,
        color = color,
        maxLines = maxLines
    )
}