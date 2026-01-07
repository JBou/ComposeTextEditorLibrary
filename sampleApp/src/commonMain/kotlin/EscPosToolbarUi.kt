import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.InvertColors
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.darkrockstudios.texteditor.CharLineOffset
import com.darkrockstudios.texteditor.TextEditorRange
import com.darkrockstudios.texteditor.richstyle.RichSpanStyle
import com.darkrockstudios.texteditor.sampleapp.EscPosConfiguration
import com.darkrockstudios.texteditor.sampleapp.EscPosExtension
import com.darkrockstudios.texteditor.sampleapp.common.FormatButton
import com.darkrockstudios.texteditor.sampleapp.common.ToolbarButton
import com.darkrockstudios.texteditor.sampleapp.common.toggleStyle
import com.darkrockstudios.texteditor.sampleapp.richstyle.DoubleUnderlineSpanStyle
import com.darkrockstudios.texteditor.state.TextEditorState
import com.darkrockstudios.texteditor.state.getRichSpansAtPosition
import com.darkrockstudios.texteditor.state.getRichSpansInRange
import com.darkrockstudios.texteditor.state.getSpanStylesInRange

val DOUBLEUNDERLINESPANSTYLE = DoubleUnderlineSpanStyle()

@Composable
fun EscPosToolbar(
    escPosExtension: EscPosExtension,
    modifier: Modifier = Modifier,
) {
    val state = remember(escPosExtension) { escPosExtension.editorState }

    var isBoldActive by remember { mutableStateOf(false) }
    var isUnderlineActive by remember { mutableStateOf(false) }
    var isDoubleUnderlineActive by remember { mutableStateOf(false) }
    var isInvertedActive by remember { mutableStateOf(false) }
    var isDoubleHeightActive by remember { mutableStateOf(false) }
    var isDoubleWidthActive by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        state.cursorDataFlow.collect { (position, cursorStyles, selection) ->
            val styles = if (selection != null) {
                state.getSpanStylesInRange(selection)
            } else {
                cursorStyles
            }

            val richSpans = if (selection != null) {
                state.getRichSpansInRange(selection)
            } else {
                state.getRichSpansAtPosition(position)
            }

            isBoldActive = styles.contains(SpanStyle(fontWeight = FontWeight.Bold))
            isUnderlineActive = styles.contains(SpanStyle(textDecoration = TextDecoration.Underline))
            
            isDoubleUnderlineActive = richSpans.any { 
                it.style is DoubleUnderlineSpanStyle 
            }
            
            // Check for inverted colors (background check is more reliable)
            val configuration = EscPosConfiguration.DEFAULT
            isInvertedActive = styles.any { it.background == configuration.invertedBackgroundColor }

            // Check for double height (fontSize * 2)
            isDoubleHeightActive = styles.any { 
                it.fontSize != TextUnit.Unspecified && 
                it.fontSize.value > configuration.defaultTextStyle.fontSize.value * 1.5f 
            }
            
            // Check for double width (letter spacing)
            isDoubleWidthActive = styles.any { 
                it.letterSpacing != TextUnit.Unspecified && 
                it.letterSpacing.value > 0.3f 
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp),
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
            }

            Spacer(modifier = Modifier.width(12.dp))

            VerticalDivider(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.width(12.dp))

            // ESC/POS Formatting Controls Group
            Row {
                // Basic formatting
                FormatButton(
                    onClick = {
                        toggleStyle(state, isBoldActive, SpanStyle(fontWeight = FontWeight.Bold))
                    },
                    icon = Icons.Default.FormatBold,
                    contentDescription = "Bold (**text**)",
                    isActive = isBoldActive,
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        toggleStyle(state, isUnderlineActive, SpanStyle(textDecoration = TextDecoration.Underline))
                    },
                    icon = Icons.Default.FormatUnderlined,
                    contentDescription = "Underline (__text__)",
                    isActive = isUnderlineActive,
                )

                Spacer(modifier = Modifier.width(4.dp))

                FormatButton(
                    onClick = {
                        toggleRichStyle(state, isDoubleUnderlineActive, DOUBLEUNDERLINESPANSTYLE)
                    },
                    icon = Icons.Default.FormatUnderlined,
                    contentDescription = "Double Underline (++text++)",
                    isActive = isDoubleUnderlineActive
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
                            SpanStyle(
                                color = configuration.invertedTextColor,
                                background = configuration.invertedBackgroundColor
                            )
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
                            SpanStyle(
                                fontSize = configuration.defaultTextStyle.fontSize * configuration.doubleHeightScale
                            )
                        )
                    },
                    icon = Icons.Default.FormatSize,
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
                            SpanStyle(
                                letterSpacing = configuration.defaultTextStyle.fontSize * configuration.doubleWidthScale
                            )
                        )
                    },
                    icon = Icons.Default.FormatSize,
                    contentDescription = "Double Width (%%text%%)",
                    isActive = isDoubleWidthActive
                )

                Spacer(modifier = Modifier.width(12.dp))

                VerticalDivider(modifier = Modifier.height(24.dp))

                Spacer(modifier = Modifier.width(12.dp))

                // Alignment controls
                ToolbarButton(
                    onClick = { insertAlignmentFormatting(state, "left") },
                    icon = Icons.AutoMirrored.Filled.FormatAlignLeft,
                    contentDescription = "Left Align (|left|text)"
                )

                Spacer(modifier = Modifier.width(4.dp))

                ToolbarButton(
                    onClick = { insertAlignmentFormatting(state, "center") },
                    icon = Icons.Default.FormatAlignCenter,
                    contentDescription = "Center Align (|center|text)"
                )

                Spacer(modifier = Modifier.width(4.dp))

                ToolbarButton(
                    onClick = { insertAlignmentFormatting(state, "right") },
                    icon = Icons.AutoMirrored.Filled.FormatAlignRight,
                    contentDescription = "Right Align (|right|text)"
                )
            }
        }
	}
}

private fun insertAlignmentFormatting(
    state: TextEditorState,
    alignment: String
) {
    // Insert alignment at the beginning of the current line
    val cursorPos = state.cursor.position
    val alignmentText = "|$alignment|"

    // Check if alignment already exists at line start
    val lineText = state.textLines[cursorPos.line].text
    if (!lineText.trim().startsWith("|")) {
        // Insert alignment at line start by replacing from beginning
        val range = TextEditorRange(
            CharLineOffset(cursorPos.line, 0),
            CharLineOffset(cursorPos.line, 0)
        )
        state.replace(range, alignmentText)
    } else {
        // Replace existing alignment
        val existingEnd = lineText.indexOf("|", 1) + 1
        if (existingEnd > 0) {
            val range = TextEditorRange(
                CharLineOffset(cursorPos.line, 0),
                CharLineOffset(cursorPos.line, existingEnd)
            )
            state.replace(range, alignmentText)
        }
    }
}

private fun toggleRichStyle(
    state: TextEditorState,
    isActive: Boolean,
    richSpanStyle: RichSpanStyle,
) {
    state.selector.selection?.let { range ->
        if (isActive) {
            state.removeRichSpan(range.start, range.end, richSpanStyle)
        } else {
            state.addRichSpan(range.start, range.end, richSpanStyle)
        }
        state.cursor.updatePosition(state.cursor.position)
    }
}
