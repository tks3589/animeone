package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aaron.chen.animeone.app.view.ui.theme.CommonMargin
import com.aaron.chen.animeone.app.view.ui.theme.Light

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    text: String,
    showBorder: Boolean = true,
    colorTint: Color = Light,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(22.dp)
            )
            .then(
                if (showBorder)
                    Modifier.border(1.dp, colorTint, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        CommonTextM(
            text = text,
            color = colorTint,
            bold = true,
            modifier = Modifier.padding(vertical = CommonMargin.m2)
        )
    }
}