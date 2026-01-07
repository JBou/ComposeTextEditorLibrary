package com.darkrockstudios.texteditor.sampleapp.richstyle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import com.darkrockstudios.texteditor.LineWrap
import com.darkrockstudios.texteditor.richstyle.RichSpanStyle

/**
 * Inverted text style - white text on black background (ESC/POS inverted)
 */
class InvertedSpanStyle(
    private val backgroundColor: Color = Color.Black,
    private val textColor: Color = Color.White
) : RichSpanStyle {
    override fun DrawScope.drawCustomStyle(
        layoutResult: TextLayoutResult,
        lineWrap: LineWrap,
        textRange: TextRange
    ) {
        val lineHeight = layoutResult.multiParagraph.getLineHeight(lineWrap.virtualLineIndex)

        val lineStartOffset = layoutResult.getLineStart(lineWrap.virtualLineIndex)
        val startX = if (textRange.start <= lineStartOffset) {
            layoutResult.getLineLeft(lineWrap.virtualLineIndex)
        } else {
            layoutResult.getHorizontalPosition(textRange.start, usePrimaryDirection = true)
        }

        val lineEndOffset = layoutResult.getLineEnd(lineWrap.virtualLineIndex, false)
        val endX = if (textRange.end >= lineEndOffset) {
            layoutResult.getLineRight(lineWrap.virtualLineIndex)
        } else {
            layoutResult.getHorizontalPosition(textRange.end, usePrimaryDirection = true)
        }

        // Draw inverted background
        drawRect(
            color = backgroundColor,
            topLeft = Offset(x = startX, y = 0f),
            size = Size(width = endX - startX, height = lineHeight)
        )
    }
}

/**
 * Double height text style (ESC/POS double height)
 */
class DoubleHeightSpanStyle(
    private val scaleFactor: Float = 2.0f
) : RichSpanStyle {
    override fun DrawScope.drawCustomStyle(
        layoutResult: TextLayoutResult,
        lineWrap: LineWrap,
        textRange: TextRange
    ) {
        // Double height is handled by SpanStyle fontSize scaling in the text style
        // This RichSpanStyle is mainly for ESC/POS export logic
    }
}

/**
 * Double width text style (ESC/POS double width)
 */
class DoubleWidthSpanStyle(
    private val scaleFactor: Float = 2.0f
) : RichSpanStyle {
    override fun DrawScope.drawCustomStyle(
        layoutResult: TextLayoutResult,
        lineWrap: LineWrap,
        textRange: TextRange
    ) {
        // Double width is handled by SpanStyle font scaling in the text style
        // This RichSpanStyle is mainly for ESC/POS export logic
    }
}

/**
 * Text alignment style (ESC/POS left/center/right alignment)
 */
class AlignmentSpanStyle(
    private val textAlign: TextAlign
) : RichSpanStyle {
    override fun DrawScope.drawCustomStyle(
        layoutResult: TextLayoutResult,
        lineWrap: LineWrap,
        textRange: TextRange
    ) {
        // Alignment is handled at the paragraph level in ESC/POS
        // This RichSpanStyle is mainly for ESC/POS export logic
        // Visual alignment in the editor is handled by SpanStyle textAlign
    }

    fun getTextAlign(): TextAlign = textAlign
}