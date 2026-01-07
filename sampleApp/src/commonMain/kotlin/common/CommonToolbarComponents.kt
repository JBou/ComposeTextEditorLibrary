package com.darkrockstudios.texteditor.sampleapp.common

import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.darkrockstudios.texteditor.state.TextEditorState

/**
 * Common toolbar button component used across different editor demos.
 * Provides consistent styling for toolbar buttons with active state support.
 */
@Composable
fun ToolbarButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    focusable: Boolean = true
) {
    FilledTonalIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(32.dp)
            .then(if (focusable) Modifier else Modifier),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
            else
                MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isActive)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Format button wrapper that uses ToolbarButton with consistent styling.
 * Use this for formatting-related buttons (bold, italic, etc.) to indicate active state.
 */
@Composable
fun FormatButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    isActive: Boolean,
    enabled: Boolean = true
) {
    ToolbarButton(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        isActive = isActive,
        enabled = enabled
    )
}

/**
 * Toggles a SpanStyle on the current selection or cursor position.
 * If there's a selection, adds/removes the style from the selected range.
 * If there's no selection, adds/removes the style from the cursor position.
 */
fun toggleStyle(
    state: TextEditorState,
    isActive: Boolean,
    spanStyle: SpanStyle
) {
    val selection = state.selector.selection
    if (selection != null) {
        if (isActive) {
            state.removeStyleSpan(selection, spanStyle)
        } else {
            state.addStyleSpan(selection, spanStyle)
        }
    } else {
        if (isActive) {
            state.cursor.removeStyle(spanStyle)
        } else {
            state.cursor.addStyle(spanStyle)
        }
    }
}
