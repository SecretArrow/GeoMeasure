package com.geomeasure.pro.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `Double formatDecimals 2 rounds to 2 decimal places`() {
        val result = 3.14159.formatDecimals(2)
        assertEquals("3.14", result)
    }

    @Test
    fun `Double formatDecimals 0 rounds to integer`() {
        val result = 3.14159.formatDecimals(0)
        assertEquals("3", result)
    }

    @Test
    fun `Double formatDecimals negative zero`() {
        assertEquals("-3.14", (-3.14159).formatDecimals(2))
    }

    @Test
    fun `String escapeJson handles double quotes`() {
        val result = """hello "world"""".escapeJson()
        assertEquals("""hello \"world\"""", result)
    }

    @Test
    fun `String escapeJson handles newlines`() {
        val result = "line1\nline2".escapeJson()
        assertEquals("line1\\nline2", result)
    }

    @Test
    fun `String escapeJson handles both quotes and newlines`() {
        val result = "say \"hello\"\nnext".escapeJson()
        assertEquals("say \\\"hello\\\"\\nnext", result)
    }

    @Test
    fun `String escapeJson normal string unchanged`() {
        assertEquals("hello", "hello".escapeJson())
    }

    @Test
    fun `String escapeXml handles ampersand`() {
        assertEquals("AT&amp;T", "AT&T".escapeXml())
    }

    @Test
    fun `String escapeXml handles less than`() {
        assertEquals("&lt;value&gt;", "<value>".escapeXml())
    }

    @Test
    fun `String escapeXml handles all three special chars`() {
        val result = "<a & b > c".escapeXml()
        assertEquals("&lt;a &amp; b &gt; c", result)
    }

    @Test
    fun `String escapeXml normal string unchanged`() {
        assertEquals("hello world", "hello world".escapeXml())
    }
}
