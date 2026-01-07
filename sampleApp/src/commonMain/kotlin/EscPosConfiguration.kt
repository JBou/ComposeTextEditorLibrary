package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Configuration for ESC/POS text formatting styles.
 * Extends basic markdown styles with ESC/POS specific formatting.
 */
data class EscPosConfiguration(
    val defaultTextStyle: SpanStyle = SpanStyle(fontSize = 16.sp),
    val boldStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val italicStyle: SpanStyle = SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
    val underlineStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),

    // ESC/POS specific styles
    val invertedBackgroundColor: Color = Color.Black,
    val invertedTextColor: Color = Color.White,
    val doubleHeightScale: Float = 2.0f,
    val doubleWidthScale: Float = 2.0f,

    // Alignment styles (for visual representation in editor)
    val centerAlignStyle: SpanStyle = SpanStyle(), // Will be handled by custom span
    val leftAlignStyle: SpanStyle = SpanStyle(),   // Will be handled by custom span
    val rightAlignStyle: SpanStyle = SpanStyle()   // Will be handled by custom span
) {
    companion object {
        val DEFAULT = EscPosConfiguration()
        val DEFAULT_DARK = DEFAULT.copy(
            invertedBackgroundColor = Color.DarkGray,
            invertedTextColor = Color.White
        )
    }
}