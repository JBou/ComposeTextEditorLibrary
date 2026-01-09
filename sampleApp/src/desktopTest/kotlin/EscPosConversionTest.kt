package com.darkrockstudios.texteditor.sampleapp

import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class EscPosConversionTest {

    @Test
    fun testStyleMarkerMappings() {
        val config = EscPosConfiguration.DEFAULT

        // Test that all expected markers are present in the config
        val expectedMarkers = setOf("**", "__", "~~", "##", "%%", "++")
        assertEquals(expectedMarkers, config.styleMarkers.keys)

        // Test that markers map to the correct styles
        assertEquals(config.boldStyle, config.styleMarkers["**"])
        assertEquals(config.underlineStyle, config.styleMarkers["__"])
        assertEquals(config.invertedStyle, config.styleMarkers["~~"])
        assertEquals(config.doubleHeightStyle, config.styleMarkers["##"])
        assertEquals(config.doubleWidthStyle, config.styleMarkers["%%"])
        assertEquals(config.shadowStyle, config.styleMarkers["++"])
    }

    @Test
    fun testAnnotatedStringToEscPosConversion() {
        val config = EscPosConfiguration.DEFAULT

        val annotatedString = buildAnnotatedString {
            append("Normal text ")
            withStyle(config.boldStyle) {
                append("bold text")
            }
            append(" ")
            withStyle(config.underlineStyle) {
                append("underline text")
            }
            append(" ")
            withStyle(config.invertedStyle) {
                append("inverted text")
            }
        }

        val escPos = annotatedString.toEscPos(config)
        assertEquals("Normal text **bold text** __underline text__ ~~inverted text~~", escPos)
    }

    @Test
    fun testEscPosToAnnotatedStringConversion() {
        val config = EscPosConfiguration.DEFAULT

        val escPosText = "Normal text **bold text** __underline text__ ~~inverted text~~"
        val annotatedString = escPosText.toEscPosAnnotatedString(config)

        // Check that we have the expected number of spans
        assertEquals(3, annotatedString.spanStyles.size)

        // Check specific spans
        val spans = annotatedString.spanStyles
        assertEquals("bold text", annotatedString.text.substring(spans[0].start, spans[0].end))
        assertEquals(config.boldStyle, spans[0].item)

        assertEquals("underline text", annotatedString.text.substring(spans[1].start, spans[1].end))
        assertEquals(config.underlineStyle, spans[1].item)

        assertEquals("inverted text", annotatedString.text.substring(spans[2].start, spans[2].end))
        assertEquals(config.invertedStyle, spans[2].item)
    }
}