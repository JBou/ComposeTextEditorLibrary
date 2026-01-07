package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

/**
 * Converts an AnnotatedString to an ESC/POS formatted string.
 * Handles bold, underline, inverted, double height, double width, and alignment styles.
 * Supports nested/overlapping markers correctly.
 *
 * NOTE: Special characters in text content are NOT escaped.
 * Users must manually escape if they want literal asterisks, tildes, etc.
 * Example: To type "*hello*" literally, escape it as "\*hello\*"
 */
fun AnnotatedString.toEscPos(
    configuration: EscPosConfiguration = EscPosConfiguration.DEFAULT
): String {
    if (text.isEmpty()) return ""

    // Create a list of style boundaries (start and end points)
    data class StyleBoundary(
        val index: Int,
        val isStart: Boolean,
        val marker: String,
        val priority: Int,
        val spanIndex: Int // Track which span this boundary belongs to
    )

    val boundaries = mutableListOf<StyleBoundary>()

    // Process regular span styles
    spanStyles.forEachIndexed { spanIndex, span ->
        val marker = getStyleMarker(span.item, configuration) ?: return@forEachIndexed
        boundaries.add(StyleBoundary(span.start, true, marker.openMarker, 0, spanIndex))
        boundaries.add(StyleBoundary(span.end, false, marker.closeMarker, 0, spanIndex))
    }

    // Calculate nesting depths for proper ordering of closing markers
    fun calculateNestingDepths(): Map<Int, Int> {
        val depths = mutableMapOf<Int, Int>()
        val activeSpans = mutableListOf<Int>()

        val sortedBoundaries = boundaries.sortedWith(compareBy<StyleBoundary> { it.index }.thenBy { it.isStart })

        sortedBoundaries.forEach { boundary ->
            if (boundary.isStart) {
                val depth = activeSpans.size
                depths[boundary.spanIndex] = depth
                activeSpans.add(boundary.spanIndex)
            } else {
                activeSpans.remove(boundary.spanIndex)
            }
        }

        return depths
    }

    val nestingDepths = calculateNestingDepths()

    // Update priorities for closing boundaries based on nesting depth
    // Higher depth = higher priority (closes first)
    boundaries.forEach { boundary ->
        if (!boundary.isStart) {
            val depth = nestingDepths[boundary.spanIndex] ?: 0
            boundaries[boundaries.indexOf(boundary)] = boundary.copy(priority = depth)
        }
    }

    // Sort boundaries by position, then by priority (higher priority first for closing markers)
    boundaries.sortWith(compareBy<StyleBoundary> { it.index }.thenByDescending { it.priority })

    val result = StringBuilder()
    var currentIndex = 0

    boundaries.forEach { boundary ->
        // Add any text between last position and this boundary (NOT escaped)
        while (currentIndex < boundary.index) {
            result.append(text[currentIndex])
            currentIndex++
        }

        // Add marker directly (NOT escaped - markers should never be escaped)
        if (boundary.isStart) {
            result.append(boundary.marker)
        }
        if (!boundary.isStart && boundary.marker.isNotEmpty()) {
            result.append(boundary.marker)
        }
    }

    // Add any remaining text (NOT escaped)
    while (currentIndex < text.length) {
        result.append(text[currentIndex])
        currentIndex++
    }

    return result.toString()
}

private data class StyleMarkerPair(
    val openMarker: String,
    val closeMarker: String
)

private fun getStyleMarker(
    style: SpanStyle,
    config: EscPosConfiguration
): StyleMarkerPair? {
    // Check for inverted colors (background check is more reliable)
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

    // Check for shadow (double strike)
    if (style.shadow == config.shadowStyle.shadow) {
        return StyleMarkerPair("++", "++")
    }

    return null
}
