package com.darkrockstudios.texteditor.sampleapp

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

    /**
     * Export the current text as ESC/POS formatted string
     */
    fun exportAsEscPosText(): String {
        val allText = editorState.getAllText()

        return allText.toEscPos(escPosConfiguration)
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
