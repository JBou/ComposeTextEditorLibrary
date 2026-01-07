package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.darkrockstudios.texteditor.sampleapp.richstyle.AlignmentSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleHeightSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleWidthSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.InvertedSpanStyle

/**
 * Convert ESC/POS formatted text to AnnotatedString for display in the editor.
 * Supports the following ESC/POS syntax:
 * - **text** : Bold (ESC/POS: emphasized)
 * - __text__ : Underline
 * - ~~text~~ : Inverted (white on black)
 * - ##text## : Double height
 * - %%text%% : Double width (approximated with letter spacing)
 * - |center|text : Center alignment (markers stripped in display)
 * - |left|text : Left alignment (markers stripped in display)
 * - |right|text : Right alignment (markers stripped in display)
 * - ++text++ : Double underline
 * 
 * Note: Alignment markers are parsed and stripped from display but preserved for ESC/POS export.
 */
fun String.toEscPosAnnotatedString(
    configuration: EscPosConfiguration = EscPosConfiguration.DEFAULT
): AnnotatedString {
    return buildAnnotatedString {
        parseEscPosText(this@toEscPosAnnotatedString, configuration)
    }
}

private fun AnnotatedString.Builder.parseEscPosText(
    text: String,
    configuration: EscPosConfiguration
) {
    var currentIndex = 0

    while (currentIndex < text.length) {
        when {
            // Bold: **text**
            text.startsWith("**", currentIndex) -> {
                val endIndex = text.indexOf("**", currentIndex + 2)
                if (endIndex != -1) {
                    withStyle(configuration.boldStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Double underline: ++text++
            /*text.startsWith("++", currentIndex) -> {
                val endIndex = text.indexOf("++", currentIndex + 2)
                if (endIndex != -1) {
                    // Since AnnotatedString doesn't support RichSpanStyle, we use a visual approximation
                    // with underline and increased line height to simulate double underline effect
                    val doubleUnderlineStyle = SpanStyle(
                        textDecoration = TextDecoration.Underline,
                        fontSize = configuration.defaultTextStyle.fontSize * 1.1f, // Slightly larger for better visibility
                        fontWeight = FontWeight.W600 // Semi-bold for thickness
                    )
                    withStyle(doubleUnderlineStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }*/

            // Underline: __text__
            text.startsWith("__", currentIndex) -> {
                val endIndex = text.indexOf("__", currentIndex + 2)
                if (endIndex != -1) {
                    withStyle(configuration.underlineStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
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
                    // For inverted text, apply both text color and background
                    val invertedStyle = SpanStyle(
                        color = configuration.invertedTextColor,
                        background = configuration.invertedBackgroundColor
                    )
                    withStyle(invertedStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
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
                    val doubleHeightStyle = SpanStyle(fontSize = configuration.defaultTextStyle.fontSize * configuration.doubleHeightScale)
                    withStyle(doubleHeightStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Double width: %%text%% (using letter spacing as approximation)
            text.startsWith("%%", currentIndex) -> {
                val endIndex = text.indexOf("%%", currentIndex + 2)
                if (endIndex != -1) {
                    // Use letter spacing to approximate double width
                    val doubleWidthStyle = SpanStyle(letterSpacing = 0.5.sp)
                    withStyle(doubleWidthStyle) {
                        append(text.substring(currentIndex + 2, endIndex))
                    }
                    currentIndex = endIndex + 2
                } else {
                    append(text[currentIndex])
                    currentIndex++
                }
            }

            // Alignment: |center|text, |left|text, |right|text
            text.startsWith("|", currentIndex) -> {
                val pipeEndIndex = text.indexOf("|", currentIndex + 1)
                if (pipeEndIndex != -1) {
                    val alignmentType = text.substring(currentIndex + 1, pipeEndIndex)
                    
                    // Check if this is a valid alignment marker
                    if (alignmentType in listOf("center", "left", "right")) {
                        val textStartIndex = pipeEndIndex + 1
                        
                        // Find end of line or next alignment marker
                        val lineEndIndex = text.indexOf("\n", textStartIndex).let { if (it == -1) text.length else it }
                        
                        // Strip the alignment markers and just show the content
                        val content = text.substring(textStartIndex, lineEndIndex)
                        append(content)
                        
                        currentIndex = lineEndIndex + if (lineEndIndex < text.length) 1 else 0
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

            // Regular text
            else -> {
                append(text[currentIndex])
                currentIndex++
            }
        }
    }
}