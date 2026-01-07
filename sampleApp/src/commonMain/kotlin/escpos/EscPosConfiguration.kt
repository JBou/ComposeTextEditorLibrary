package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

/**
 * Configuration for ESC/POS text formatting styles.
 * Extends basic markdown styles with ESC/POS specific formatting.
 */
data class EscPosConfiguration(
    val defaultTextStyle: SpanStyle = SpanStyle(fontSize = 16.sp),
    val boldStyle: SpanStyle = SpanStyle(fontWeight = FontWeight.Bold),
    val italicStyle: SpanStyle = SpanStyle(fontStyle = FontStyle.Italic),
    val underlineStyle: SpanStyle = SpanStyle(textDecoration = TextDecoration.Underline),

    // ESC/POS specific styles
    val invertedBackgroundColor: Color = Color.Black,
    val invertedTextColor: Color = Color.White,
    val invertedStyle: SpanStyle = SpanStyle(
        color = invertedTextColor,
        background = invertedBackgroundColor
    ),
    val doubleHeightScale: Float = 1.2f,
    val doubleHeightStyle: SpanStyle = SpanStyle(
        fontSize = defaultTextStyle.fontSize * doubleHeightScale
    ),
    val doubleWidthScale: Float = 0.5f,
    val doubleWidthStyle: SpanStyle = SpanStyle(
        letterSpacing = defaultTextStyle.fontSize * doubleWidthScale
    ),
    val shadowOffset: Float = 1.5f,
    val shadowBlurRadius: Float = 0f,
    val shadowStyle: SpanStyle = SpanStyle(
        shadow = Shadow(
            offset = Offset(shadowOffset, 0f), // horizontal offset
            blurRadius = shadowBlurRadius
        )
    ),

    // Alignment styles (for visual representation in editor)
    val centerAlignStyle: SpanStyle = SpanStyle(), // Will be handled by custom span
    val leftAlignStyle: SpanStyle = SpanStyle(),   // Will be handled by custom span
    val rightAlignStyle: SpanStyle = SpanStyle(),  // Will be handled by custom span

    // Format marker style for alignment markers (semi-transparent)
    val formatMarkerStyle: SpanStyle = SpanStyle(color = Color.Black.copy(alpha = 0.2f))
) {
    companion object {
        val DEFAULT = EscPosConfiguration()
    }
}