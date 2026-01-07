package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.darkrockstudios.texteditor.sampleapp.richstyle.AlignmentSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleUnderlineSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleHeightSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleWidthSpanStyle

/**
 * Converts ESC/POS formatted text to AnnotatedString for display in editor.
 * Supports nested/overlapping markers using stack-based parsing.
 *
 * Supported syntax:
 * - **text** : Bold
 * - __text__ : Underline
 * - ~~text~~ : Inverted (white on black)
 * - ##text## : Double height
 * - %%text%% : Double width
 * - ++text++ : Double underline
 * - |center|text : Center alignment (markers stripped in display)
 * - |left|text : Left alignment (markers stripped in display)
 * - |right|text : Right alignment (markers stripped in display)
 *
 * Note: Alignment markers are parsed and stripped from display but preserved for ESC/POS export.
 * Note: Markers can be nested and overlapping. Styles are applied in reverse order (last closed = first applied).
 */
fun String.toEscPosAnnotatedString(
    configuration: EscPosConfiguration = EscPosConfiguration.DEFAULT
): AnnotatedString {
    return buildAnnotatedString {
        parseEscPosTextWithStack(this@toEscPosAnnotatedString, configuration)
    }
}

/**
 * Stack-based parser for handling nested/overlapping markers.
 * Uses a marker stack to track currently open styles and applies them when closed.
 */
private fun AnnotatedString.Builder.parseEscPosTextWithStack(
    text: String,
    configuration: EscPosConfiguration
) {
    data class ActiveStyle(val marker: String, val startIndex: Int, val spanStyle: SpanStyle?)
    val styleStack = mutableListOf<ActiveStyle>()

    var currentIndex = 0

    while (currentIndex < text.length) {
        when {
            // Alignment: |left|text, |center|text, |right|text
            text.startsWith("|", currentIndex) -> {
                val pipeEndIndex = text.indexOf("|", currentIndex + 1)
                if (pipeEndIndex != -1) {
                    val alignmentType = text.substring(currentIndex + 1, pipeEndIndex)
                    if (alignmentType in listOf("left", "center", "right")) {
                        // Store alignment for export
                        styleStack.add(ActiveStyle("|$alignmentType|", currentIndex, null))
                        
                        // Skip alignment markers and just show content
                        currentIndex = pipeEndIndex + 1
                    } else {
                        // Not a valid alignment marker, treat as regular text
                        append(text[currentIndex])
                        currentIndex++
                    }
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Bold: **text**
            text.startsWith("**", currentIndex) -> {
                val endIndex = text.indexOf("**", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("**", currentIndex, SpanStyle(fontWeight = FontWeight.Bold)))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Underline: __text__
            text.startsWith("__", currentIndex) -> {
                val endIndex = text.indexOf("__", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("__", currentIndex, SpanStyle(textDecoration = TextDecoration.Underline)))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Inverted: ~~text~~
            text.startsWith("~~", currentIndex) -> {
                val endIndex = text.indexOf("~~", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("~~", currentIndex, SpanStyle(
                        color = configuration.invertedTextColor,
                        background = configuration.invertedBackgroundColor
                    )))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Double height: ##text##
            text.startsWith("##", currentIndex) -> {
                val endIndex = text.indexOf("##", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("##", currentIndex, SpanStyle(
                        fontSize = configuration.defaultTextStyle.fontSize * configuration.doubleHeightScale
                    )))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Double width: %%text%%
            text.startsWith("%%", currentIndex) -> {
                val endIndex = text.indexOf("%%", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("%%", currentIndex, SpanStyle(
                        letterSpacing = configuration.defaultTextStyle.fontSize * configuration.doubleWidthScale
                    )))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Double underline: ++text++
            text.startsWith("++", currentIndex) -> {
                val endIndex = text.indexOf("++", currentIndex + 2)
                if (endIndex != -1) {
                    styleStack.add(ActiveStyle("++", currentIndex, SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                        fontSize = configuration.defaultTextStyle.fontSize * 1.05f
                    )))
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Regular text (not part of any marker)
            else -> {
                append(text[currentIndex])
                currentIndex++
            }
        }
    }
}
