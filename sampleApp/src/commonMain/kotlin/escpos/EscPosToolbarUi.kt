package escpos

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import com.darkrockstudios.texteditor.CharLineOffset
import com.darkrockstudios.texteditor.TextEditorRange
import com.darkrockstudios.texteditor.contextmenu.TextEditorContextMenuState
import com.darkrockstudios.texteditor.sampleapp.EscPosConfiguration
import com.darkrockstudios.texteditor.sampleapp.EscPosExtension
import com.darkrockstudios.texteditor.state.TextEditorState
import com.darkrockstudios.texteditor.state.getSpanStylesInRange

@Composable
fun EscPosToolbar(
    escPosExtension: EscPosExtension,
    modifier: Modifier = Modifier,
    contextMenuState: TextEditorContextMenuState? = null,
) {
    val state = remember(escPosExtension) { escPosExtension.editorState }

    var isBoldActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }
    var isDoubleStrikeActive by remember { mutableStateOf(false) }
    var isInvertedActive by remember { mutableStateOf(false) }
    var isDoubleHeightActive by remember { mutableStateOf(false) }
    var isDoubleWidthActive by remember { mutableStateOf(false) }

    var isLeftAlignActive by remember { mutableStateOf(false) }
    var isCenterAlignActive by remember { mutableStateOf(false) }
    var isRightAlignActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        state.cursorDataFlow.collect { (position, cursorStyles, selection) ->
            val styles = if (selection != null) {
                state.getSpanStylesInRange(selection)
            } else {
                cursorStyles
            }

            val configuration = EscPosConfiguration.DEFAULT
            isBoldActive = styles.contains(configuration.boldStyle)
            isUnderlineActive = styles.contains(configuration.underlineStyle)
            isDoubleStrikeActive = styles.contains(configuration.shadowStyle)
            // Check for inverted colors (background check is more reliable)
            isInvertedActive = styles.contains(configuration.invertedStyle)

            // Check for double height (fontSize * 2)
            isDoubleHeightActive = styles.contains(configuration.doubleHeightStyle)
            
            // Check for double width (letter spacing)
            isDoubleWidthActive = styles.contains(configuration.doubleWidthStyle)

            //commit Check for alignment markers on current line
            val currentLine = state.textLines.getOrNull(position.line)?.text ?: ""
            isLeftAlignActive = currentLine.trim().startsWith("|left|")
            isCenterAlignActive = currentLine.trim().startsWith("|center|")
            isRightAlignActive = currentLine.trim().startsWith("|right|")
        }
    }

    Surface(
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 2.dp)
                .wrapContentWidth()
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // History Controls Group
            Row {
                ToolbarButton(
                    onClick = state::undo,
                    icon = Icons.AutoMirrored.Filled.Undo,
                    contentDescription = "Undo",
                    enabled = state.canUndo
                )

                Spacer(modifier = Modifier.width(4.dp))

                ToolbarButton(
                    onClick = state::redo,
                    icon = Icons.AutoMirrored.Filled.Redo,
                    contentDescription = "Redo",
                    enabled = state.canRedo
                )

                if (contextMenuState != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    ToolbarButton(
                        onClick = { 
                            val targetPosition = state.selector.selection?.end ?: state.cursorPosition
                            val cursorMetrics = state.getPositionForOffset(targetPosition)
                            val menuOffset = cursorMetrics.position + Offset(0f, 35f) // Add some vertical buffer like wordVisibilityBuffer
                            contextMenuState.showMenu(menuOffset)
                        },
                        icon = Icons.Filled.MoreVert,
                        contentDescription = "Show context menu"
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            VerticalDivider(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.width(12.dp))

            // ESC/POS Formatting Controls Group
            Row {
                // Basic formatting
                FormatButton(
                    onClick = {
                        toggleStyle(state, isBoldActive, EscPosConfiguration.DEFAULT.boldStyle)
                    },
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Bold (**text**)",
                    isActive = isBoldActive,
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        toggleStyle(state, isUnderlineActive, EscPosConfiguration.DEFAULT.underlineStyle)
                    },
                    icon = Icons.Default.FormatUnderlined,
                    contentDescription = "Underline (__text__)",
                    isActive = isUnderlineActive,
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        toggleStyle(state, isDoubleStrikeActive, EscPosConfiguration.DEFAULT.shadowStyle)
                    },
                    icon = Icons.Default.FormatItalic,
                    contentDescription = "Double Strike (++text++)",
                    isActive = isDoubleStrikeActive
                )

                Spacer(modifier = Modifier.width(12.dp))

                VerticalDivider(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.width(12.dp))

                // ESC/POS specific formatting
                FormatButton(
                    onClick = {
                        val configuration = EscPosConfiguration.DEFAULT
                        toggleStyle(
                            state, 
                            isInvertedActive, 
                            configuration.invertedStyle
                        )
                    },
                    icon = Icons.Default.InvertColors,
                    contentDescription = "Inverted (~~text~~)",
                    isActive = isInvertedActive
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        val configuration = EscPosConfiguration.DEFAULT
                        toggleStyle(
                            state, 
                            isDoubleHeightActive, 
                            configuration.doubleHeightStyle
                        )
                    },
                    icon = Icons.Default.SwapVert,
                    contentDescription = "Double Height (##text##)",
                    isActive = isDoubleHeightActive
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        val configuration = EscPosConfiguration.DEFAULT
                        toggleStyle(
                            state, 
                            isDoubleWidthActive, 
                            configuration.doubleWidthStyle
                        )
                    },
                    icon = Icons.Default.SwapHoriz,
                    contentDescription = "Double Width (%%text%%)",
                    isActive = isDoubleWidthActive
                )

                Spacer(modifier = Modifier.width(12.dp))

                VerticalDivider(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.width(12.dp))

                // Alignment controls
                FormatButton(
                    onClick = { insertAlignmentFormatting(state, "left", isLeftAlignActive) },
                    icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                    contentDescription = "Left Align (|left|text)",
                    isActive = isLeftAlignActive
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = { insertAlignmentFormatting(state, "center", isCenterAlignActive) },
                    icon = Icons.Default.FormatAlignCenter,
                    contentDescription = "Center Align (|center|text)",
                    isActive = isCenterAlignActive
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = { insertAlignmentFormatting(state, "right", isRightAlignActive) },
                    icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                    contentDescription = "Right Align (|right|text)",
                    isActive = isRightAlignActive
                )
            }
        }
	}
}

private fun insertAlignmentFormatting(
    state: TextEditorState,
    alignment: String,
    isActive: Boolean
) {
    val line = state.cursor.position.line
    val lineText = state.textLines[line].text
    val selection = state.selector.selection
    val existingEnd = lineText.takeIf { it.trim().startsWith("|") }
        ?.indexOf("|", 1)
        ?.takeIf { it >= 0 }
        ?.plus(1)

    fun replace(rangeEnd: Int, replacement: String) {
        val range = TextEditorRange(
            CharLineOffset(line, 0),
            CharLineOffset(line, rangeEnd)
        )
        state.replace(range, replacement)

        // Apply format marker style to alignment markers
        if (replacement.startsWith("|") && replacement.endsWith("|")) {
            val markerRange = TextEditorRange(
                CharLineOffset(line, 0),
                CharLineOffset(line, replacement.length)
            )
            state.addStyleSpan(markerRange, EscPosConfiguration.DEFAULT.formatMarkerStyle)
        }

        selection?.let {
            val delta = replacement.length - rangeEnd
            state.selector.updateSelection(
                CharLineOffset(it.start.line, maxOf(0, it.start.char + delta)),
                CharLineOffset(it.end.line, maxOf(0, it.end.char + delta))
            )
        }
    }

    when {
        isActive && existingEnd != null ->
            replace(existingEnd, "")

        !isActive -> {
            val alignmentText = "|$alignment|"
            replace(existingEnd ?: 0, alignmentText)
        }
    }
}


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
