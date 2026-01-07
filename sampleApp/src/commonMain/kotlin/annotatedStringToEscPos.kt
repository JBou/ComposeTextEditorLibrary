package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.darkrockstudios.texteditor.CharLineOffset
import com.darkrockstudios.texteditor.TextEditorRange
import com.darkrockstudios.texteditor.sampleapp.richstyle.AlignmentSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleUnderlineSpanStyle

/**
 * Converts an AnnotatedString to an ESC/POS formatted string.
 * Handles bold, underline, inverted, double height, double width, and alignment styles.
 * Only converts styles that match our supported ESC/POS styles.
 */
fun AnnotatedString.toEscPos(
    configuration: EscPosConfiguration = EscPosConfiguration.DEFAULT,
    richSpanInfo: List<RichSpanExportInfo> = emptyList()
): String {
    if (text.isEmpty()) return ""

    // Create a list of style boundaries (start and end points)
    data class StyleBoundary(
        val index: Int,
        val isStart: Boolean,
        val marker: StyleMarkerPair,
        val priority: Int
    )

    val boundaries = mutableListOf<StyleBoundary>()

    // Process regular span styles
    spanStyles.forEach { span ->
        val marker = getStyleMarker(span.item, configuration) ?: return@forEach
        boundaries.add(StyleBoundary(span.start, true, marker, 0))
        boundaries.add(StyleBoundary(span.end, false, marker, 0))
    }

    // Process rich span styles (double underline, alignment)
    richSpanInfo.forEach { richSpan ->
        when (richSpan.style) {
            is DoubleUnderlineSpanStyle -> {
                boundaries.add(StyleBoundary(richSpan.start, true, StyleMarkerPair("++", "++"), 1))
                boundaries.add(StyleBoundary(richSpan.end, false, StyleMarkerPair("++", "++"), 1))
            }
            is AlignmentSpanStyle -> {
                val alignTag = when (richSpan.style.getTextAlign()) {
                    androidx.compose.ui.text.style.TextAlign.Left -> "|left|"
                    androidx.compose.ui.text.style.TextAlign.Center -> "|center|"
                    androidx.compose.ui.text.style.TextAlign.Right -> "|right|"
                    else -> "|left|"
                }
                // Alignment is just a prefix at the line start
                boundaries.add(StyleBoundary(richSpan.start, true, StyleMarkerPair(alignTag, ""), 1))
            }
        }
    }

    // Sort boundaries by position, then by priority (rich spans first)
    boundaries.sortWith(compareBy<StyleBoundary> { it.index }.thenBy { it.priority })

    val result = StringBuilder()
    var currentIndex = 0

    boundaries.forEach { boundary ->
        // Add any text between last position and this boundary
        while (currentIndex < boundary.index) {
            result.append(escapeEscPosChar(text[currentIndex]))
            currentIndex++
        }

        // Add appropriate marker
        if (boundary.isStart) {
            result.append(boundary.marker.openMarker)
        } else {
            result.append(boundary.marker.closeMarker)
        }
    }

    // Add any remaining text
    while (currentIndex < text.length) {
        result.append(escapeEscPosChar(text[currentIndex]))
        currentIndex++
    }

    return result.toString()
}

/**
 * Data class for rich span export information
 */
data class RichSpanExportInfo(
    val start: Int,
    val end: Int,
    val style: com.darkrockstudios.texteditor.richstyle.RichSpanStyle
)

private fun getStyleMarker(
    style: SpanStyle,
    config: EscPosConfiguration
): StyleMarkerPair? {
    // Check for inverted colors (background color check)
    if (style.background == config.invertedBackgroundColor) {
        return StyleMarkerPair("~~", "~~")
    }

    // Check for bold
    if (style.fontWeight?.weight == 700 || style.fontWeight == FontWeight.Bold) {
        return StyleMarkerPair("**", "**")
    }

    // Check for underline
    if (style.textDecoration == TextDecoration.Underline) {
        return StyleMarkerPair("__", "__")
    }

    // Check for double height
    if (style.fontSize.value > config.defaultTextStyle.fontSize.value * 1.2f) {
        return StyleMarkerPair("##", "##")
    }

    // Check for double width
    if (style.letterSpacing.value > 0.1f) {
        return StyleMarkerPair("%%", "%%")
    }

    return null
}

/**
 * Escapes special characters that could be misinterpreted as ESC/POS markup.
 * Characters that need escaping: *, _, ~, #, %, +, |, \
 */
private fun escapeEscPosChar(char: Char): String {
    return when (char) {
        '*', '_', '~', '#', '%', '+', '|', '\\' -> "\\$char"
        else -> char.toString()
    }
}

private data class StyleMarkerPair(
    val openMarker: String,
    val closeMarker: String
)
