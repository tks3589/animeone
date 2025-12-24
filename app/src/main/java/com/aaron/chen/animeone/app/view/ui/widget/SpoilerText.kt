package com.aaron.chen.animeone.app.view.ui.widget

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.aaron.chen.animeone.app.view.ui.theme.FontSize
import com.aaron.chen.animeone.app.view.ui.theme.dpTextUnit


@Composable
fun SpoilerText(
    text: String,
    modifier: Modifier = Modifier
) {
    val parts = remember(text) { parseSpoilerText(text) }
    val revealedMap = rememberSaveable(
        text,
        saver = mapSaver(
            save = { stateMap ->
                stateMap.mapKeys { it.key.toString() }
            },
            restore = { restored ->
                val map = mutableStateMapOf<Int, Boolean>()
                restored.forEach { (key, value) ->
                    map[key.toInt()] = value as Boolean
                }
                map
            }
        )
    ) {
        mutableStateMapOf()
    }

    val annotatedText = buildAnnotatedString {
        parts.forEach { part ->
            when (part) {
                is SpoilerTextPart.Normal -> {
                    append(part.text)
                }

                is SpoilerTextPart.Spoiler -> {
                    val isRevealed = revealedMap[part.index] == true
                    val displayText = if (isRevealed) {
                        part.text
                    } else {
                        "▇".repeat(part.text.length)
                    }

                    val start = length
                    append(displayText)
                    val end = length

                    addStyle(
                        style = SpanStyle(
                            background = MaterialTheme.colorScheme.outline
                        ),
                        start = start,
                        end = end
                    )

                    addStringAnnotation(
                        tag = "SPOILER",
                        annotation = part.index.toString(),
                        start = start,
                        end = end
                    )
                }
            }
        }
    }

    ClickableText(
        text = annotatedText,
        modifier = modifier,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = dpTextUnit(FontSize.s)
        )
    ) { offset ->
        annotatedText
            .getStringAnnotations("SPOILER", offset, offset)
            .firstOrNull()
            ?.let { annotation ->
                val index = annotation.item.toInt()
                revealedMap[index] = true
            }
    }
}

fun parseSpoilerText(text: String): List<SpoilerTextPart> {
    val regex = Regex(
        "<spoiler>(.*?)</spoiler>",
        setOf(RegexOption.DOT_MATCHES_ALL)
    )
    val result = mutableListOf<SpoilerTextPart>()

    var lastIndex = 0
    var spoilerIndex = 0

    regex.findAll(text).forEach { match ->
        val start = match.range.first
        val end = match.range.last + 1

        if (start > lastIndex) {
            result += SpoilerTextPart.Normal(text.substring(lastIndex, start))
        }

        result += SpoilerTextPart.Spoiler(
            text = match.groupValues[1],
            index = spoilerIndex++
        )

        lastIndex = end
    }

    if (lastIndex < text.length) {
        result += SpoilerTextPart.Normal(text.substring(lastIndex))
    }

    return result
}


sealed class SpoilerTextPart {
    data class Normal(val text: String) : SpoilerTextPart()
    data class Spoiler(val text: String, val index: Int) : SpoilerTextPart()
}