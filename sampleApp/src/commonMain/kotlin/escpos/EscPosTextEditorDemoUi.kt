package escpos

import Destination
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.darkrockstudios.texteditor.TextEditor
import com.darkrockstudios.texteditor.rememberTextEditorStyle
import com.darkrockstudios.texteditor.state.SpanClickType
import com.darkrockstudios.texteditor.state.TextEditorState
import com.darkrockstudios.texteditor.state.rememberTextEditorState
import com.darkrockstudios.texteditor.sampleapp.EscPosConfiguration
import com.darkrockstudios.texteditor.sampleapp.toEscPosAnnotatedString
import com.darkrockstudios.texteditor.sampleapp.withEscPos

@Composable
fun EscPosTextEditorDemoUi(
    modifier: Modifier = Modifier,
    navigateTo: (Destination) -> Unit,
) {
    val configuration = remember { EscPosConfiguration.DEFAULT }
    val fixedText = createEscPosDemoText()
    val state: TextEditorState = rememberTextEditorState(fixedText.toEscPosAnnotatedString(configuration))
    val escPosExtension = remember(state, configuration) { state.withEscPos(configuration) }

    // State for exported markup display
    var exportedMarkup by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        exportedMarkup = escPosExtension.exportAsEscPosText()
        state.editOperations.collect { _ ->
            exportedMarkup = escPosExtension.exportAsEscPosText()
        }
    }

    Column(modifier = modifier) {
        Row {
            Text(
                "ESC/POS Text Editor",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { navigateTo(Destination.Menu) }) {
                Text("X")
            }
        }

        EscPosToolbar(
            escPosExtension = escPosExtension
        )

        val style = rememberTextEditorStyle(
            placeholderText = "Enter ESC/POS formatted text here...",
            textColor = MaterialTheme.colorScheme.onSurface,
        )

        TextEditor(
            state = state,
            modifier = Modifier
                .padding(8.dp)
                .weight(1f), 
            style = style,
            onRichSpanClick = { span, clickType, _ ->
                when (clickType) {
                    SpanClickType.TAP -> println("ESC/POS span tap: $span")
                    SpanClickType.PRIMARY_CLICK -> println("ESC/POS span left click: $span")
                    SpanClickType.SECONDARY_CLICK -> println("ESC/POS span right click: $span")
                }
                true
            }
        )

        // Area for exported markup display - always visible
        Text(
            "Exported Markup:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, top = 16.dp)
        )
        OutlinedTextField(
            value = exportedMarkup,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .padding(8.dp)
                .heightIn(min = 100.dp, max = 200.dp)
                .verticalScroll(scrollState)
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant
            ),
            placeholder = { Text("No content to export") }
        )
    }
}

/**
 * Sample ESC/POS formatted text demonstrating the syntax
 */
private fun createEscPosDemoText(): String = """
|center|Welcome to Our Store!
~~**IMPORTANT NOTICE**~~

##Today's Specials##
**Premium Coffee** - $3.99
++Latte++ - $4.49

|right|Thank you for visiting!
__Please come again__
""".trimIndent()