package com.darkrockstudios.texteditor.sampleapp.richstyle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange
import com.darkrockstudios.texteditor.LineWrap
import com.darkrockstudios.texteditor.richstyle.RichSpanStyle

/**
 * Double underline style for ESC/POS formatting
 * Draws two underline lines below the text
 */
class DoubleUnderlineSpanStyle(
    private val lineColor: Color = Color.Black,
    private val lineThickness: Float = 1.5f,
    private val lineSpacing: Float = 2f
) : RichSpanStyle {
    override fun DrawScope.drawCustomStyle(
        layoutResult: TextLayoutResult,
        lineWrap: LineWrap,
        textRange: TextRange
    ) {
        val baseline = layoutResult.multiParagraph.getLineBottom(lineWrap.virtualLineIndex)
        val lineHeight = layoutResult.multiParagraph.getLineHeight(lineWrap.virtualLineIndex)
        
        // Calculate horizontal bounds
        val startX = layoutResult.getHorizontalPosition(textRange.start, usePrimaryDirection = true)
        val endX = layoutResult.getHorizontalPosition(textRange.end, usePrimaryDirection = true)
        
        val y1 = baseline + lineSpacing  // First underline
        val y2 = baseline + lineSpacing * 2  // Second underline
        
        // Draw first underline
        drawLine(
            color = lineColor,
            start = Offset(startX, y1),
            end = Offset(endX, y1),
            strokeWidth = lineThickness
        )
        
        // Draw second underline
        drawLine(
            color = lineColor,
            start = Offset(startX, y2),
            end = Offset(endX, y2),
            strokeWidth = lineThickness
        )
    }
}