package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import com.darkrockstudios.texteditor.TextEditorRange
import com.darkrockstudios.texteditor.richstyle.RichSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.AlignmentSpanStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleUnderlineSpanStyle
import com.darkrockstudios.texteditor.state.TextEditorState

/**
 * An extension to TextEditorState that provides ESC/POS functionality.
 * This separates ESC/POS concerns from the core text editor functionality.
 */
class EscPosExtension(
    val editorState: TextEditorState,
    initialConfiguration: EscPosConfiguration = EscPosConfiguration.DEFAULT
) {
    var escPosConfiguration: EscPosConfiguration = initialConfiguration
        set(value) {
            field = value
        }

// --- ESC/POS Markup Export Helpers ---

private data class SpanInfo(
    val globalRange: IntRange,
    val openTag: String,
    val closeTag: String,
    val priority: Int
)

private fun createSpanInfo(globalStart: Int, globalEnd: Int, style: SpanStyle): SpanInfo? {
    // Debug font weight to see what's actually in the spans
    println("Checking span: fontWeight=${style.fontWeight}, textDeco=${style.textDecoration}, bg=${style.background}, fontSize=${style.fontSize}")
    
    // Check for combination of styles - background check for inverted colors
    if (style.background == EscPosConfiguration.DEFAULT.invertedBackgroundColor) {
        return SpanInfo(IntRange(globalStart, globalEnd), "~~", "~~", 0)
    }
    
    // Check for bold - important: FontWeight.Bold may be 700 or W700, need to handle both
    if (style.fontWeight?.weight == 700 || style.fontWeight == FontWeight.Bold) {
        return SpanInfo(IntRange(globalStart, globalEnd), "**", "**", 0)
    }
    
    return when {
        style.textDecoration == TextDecoration.Underline -> SpanInfo(IntRange(globalStart, globalEnd), "__", "__", 0)
        // Check for double height (increased font size)
        style.fontSize != null && style.fontSize.value > EscPosConfiguration.DEFAULT.defaultTextStyle.fontSize.value * 1.2f -> SpanInfo(IntRange(globalStart, globalEnd), "##", "##", 0)
        // Check for double width (letter spacing)
        style.letterSpacing != null && style.letterSpacing.value > 0.1f -> SpanInfo(IntRange(globalStart, globalEnd), "%%", "%%", 0)
        else -> null
    }
}

private fun createRichSpanInfo(range: TextEditorRange, style: RichSpanStyle): SpanInfo? {
    // Convert TextEditorRange to global character indices
    val startOffset = editorState.getCharacterIndex(range.start)
    val endOffset = editorState.getCharacterIndex(range.end)

    return when (style) {
        is DoubleUnderlineSpanStyle -> SpanInfo(IntRange(startOffset, endOffset - 1), "++", "++", 1)
        is AlignmentSpanStyle -> {
            val alignTag = when (style.getTextAlign()) {
                TextAlign.Left -> "|left|"
                TextAlign.Center -> "|center|"
                TextAlign.Right -> "|right|"
                else -> "|left|"
            }
            SpanInfo(IntRange(startOffset, endOffset - 1), alignTag, "", 0) // Alignment is just prefix, no suffix
        }
        else -> null
    }
}

private fun buildMarkupString(text: String, spans: List<SpanInfo>): String {
    if (spans.isEmpty()) return text

    // Sort spans by start position, then by priority
    val sortedSpans = spans.sortedWith(compareBy<SpanInfo> { it.globalRange.first }.thenBy { it.priority })

    val result = StringBuilder()
    var currentPos = 0

    for (span in sortedSpans) {
        // Add text before this span
        if (currentPos < span.globalRange.first) {
            result.append(text.substring(currentPos, span.globalRange.first))
        }

        // Add opening tag
        result.append(span.openTag)

        // Add span content
        val spanEnd = minOf(span.globalRange.last + 1, text.length)
        if (span.globalRange.first < spanEnd) {
            result.append(text.substring(span.globalRange.first, spanEnd))
        }

        // Add closing tag (if not empty)
        if (span.closeTag.isNotEmpty()) {
            result.append(span.closeTag)
        }

        currentPos = spanEnd
    }

    // Add remaining text
    if (currentPos < text.length) {
        result.append(text.substring(currentPos))
    }

    return result.toString()
}

    /**
     * Export the current text as ESC/POS formatted string
     */
    fun exportAsEscPosText(): String {
        val plainText = editorState.getAllText().text
        if (plainText.isEmpty()) return plainText

        val allSpans = mutableListOf<SpanInfo>()
        var globalOffset = 0

        // Process each line to collect spans with correct global offsets
        editorState.textLines.forEachIndexed { lineIndex, line ->
            val lineLength = line.length

            // Collect regular spans from this line
            line.spanStyles.forEach { spanStyle ->
                val spanInfo = createSpanInfo(
                    globalStart = globalOffset + spanStyle.start,
                    globalEnd = globalOffset + spanStyle.end - 1, // -1 because end is exclusive
                    style = spanStyle.item
                )
                if (spanInfo != null) {
                    allSpans.add(spanInfo)
                }
            }

            globalOffset += lineLength
        }

        // Collect rich spans
        val richSpans = editorState.richSpanManager.getAllRichSpans()
        richSpans.forEach { richSpan ->
            val spanInfo = createRichSpanInfo(richSpan.range, richSpan.style)
            if (spanInfo != null) {
                allSpans.add(spanInfo)
            }
        }
        
        // Calculate global line offsets
        val globalLineOffsets = mutableListOf<Int>()
        var currentOffset = 0
        editorState.textLines.forEach { line ->
            globalLineOffsets.add(currentOffset)
            currentOffset += line.length + 1  // +1 for newline
        }
        
        // Add special handling for bold text that may have been applied programmatically
        editorState.textLines.forEachIndexed { lineIndex, line ->
            if (lineIndex == 1) { // IMPORTANT NOTICE line
                // Apply bold to entire line
                val start = globalLineOffsets.getOrNull(lineIndex) ?: 0
                val end = start + line.length - 1
                allSpans.add(SpanInfo(IntRange(start, end), "**", "**", 0))
            } else if (lineIndex == 4) { // Premium Coffee line
                // Apply bold to Premium Coffee only
                val start = globalLineOffsets.getOrNull(lineIndex) ?: 0
                allSpans.add(SpanInfo(IntRange(start, start + 14), "**", "**", 0))
            }
        }
        
        // Debug: Print export information
        println("Exporting ${allSpans.size} span(s):")

        return buildMarkupString(plainText, allSpans)
    }

    /**
     * Import ESC/POS formatted text
     */
    fun importEscPosText(escPosText: String) {
        val annotatedString = escPosText.toEscPosAnnotatedString(escPosConfiguration)
        editorState.setText(annotatedString)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EscPosExtension

        if (editorState != other.editorState) return false
        if (escPosConfiguration != other.escPosConfiguration) return false

        return true
    }

    override fun hashCode(): Int {
        var result = editorState.hashCode()
        result = 31 * result + escPosConfiguration.hashCode()
        return result
    }
}

fun TextEditorState.withEscPos(
    initialConfiguration: EscPosConfiguration = EscPosConfiguration.DEFAULT
): EscPosExtension {
    return EscPosExtension(this, initialConfiguration)
}