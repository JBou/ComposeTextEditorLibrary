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
 * Parses each marker pair individually, applying styles to text between opening and closing markers.
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
 * Parses ESC/POS formatted text and applies appropriate styles.
 * Uses a recursive approach to handle nested markers correctly.
 */
private fun AnnotatedString.Builder.parseEscPosTextWithStack(
    text: String,
    configuration: EscPosConfiguration
) {
    parseEscPosTextRecursive(text, 0, text.length, configuration)
}

private fun AnnotatedString.Builder.parseEscPosTextRecursive(
    text: String,
    start: Int,
    end: Int,
    configuration: EscPosConfiguration
) {
    var currentIndex = start
    
    while (currentIndex < end) {
        when {
            // Alignment markers - skip them entirely
            text.startsWith("|", currentIndex) -> {
                val pipeEndIndex = text.indexOf("|", currentIndex + 1)
                if (pipeEndIndex != -1 && pipeEndIndex < end) {
                    val alignmentType = text.substring(currentIndex + 1, pipeEndIndex)
                    if (alignmentType in listOf("left", "center", "right")) {
                        currentIndex = pipeEndIndex + 1
                        continue
                    }
                }
                append(text[currentIndex])
                currentIndex++
            }
            
            // Check for any marker at current position
            else -> {
                val marker = findMarkerAtPosition(text, currentIndex, end)
                if (marker != null) {
                    val (markerType, markerEnd) = marker
                    
                    // Apply the style to the content between markers
                    val contentStart = currentIndex + 2
                    val contentEnd = markerEnd - 2
                    
                    if (contentStart < contentEnd) {
                        withStyle(getStyleForMarker(markerType, configuration)) {
                            // Recursively parse the content between markers
                            parseEscPosTextRecursive(text, contentStart, contentEnd, configuration)
                        }
                    }
                    
                    currentIndex = markerEnd
                } else {
                    // Regular text
                    append(text[currentIndex])
                    currentIndex++
                }
            }
        }
    }
}

private fun findMarkerAtPosition(text: String, index: Int, maxEnd: Int): Pair<String, Int>? {
    val markers = listOf("**", "__", "~~", "##", "%%", "++")
    
    for (marker in markers) {
        if (text.startsWith(marker, index)) {
            val endIndex = text.indexOf(marker, index + 2)
            if (endIndex != -1 && endIndex + marker.length <= maxEnd) {
                return Pair(marker, endIndex + marker.length)
            }
        }
    }
    return null
}

private fun getStyleForMarker(marker: String, config: EscPosConfiguration): SpanStyle {
    return when (marker) {
        "**" -> SpanStyle(fontWeight = FontWeight.Bold)
        "__" -> SpanStyle(textDecoration = TextDecoration.Underline)
        "~~" -> SpanStyle(
            color = config.invertedTextColor,
            background = config.invertedBackgroundColor
        )
        "##" -> SpanStyle(fontSize = config.defaultTextStyle.fontSize * config.doubleHeightScale)
        "%%" -> SpanStyle(letterSpacing = config.defaultTextStyle.fontSize * config.doubleWidthScale)
        "++" -> SpanStyle(
            textDecoration = TextDecoration.Underline,
            fontWeight = FontWeight.Bold,
            fontSize = config.defaultTextStyle.fontSize * 1.05f
        )
        else -> SpanStyle()
    }
}
