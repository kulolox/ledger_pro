package com.example.back_end.unit.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.example.back_end.utils.SanitizeUtils;
import org.junit.jupiter.api.Test;

class SanitizeUtilsTest {

    @Test
    void escapeHtml_shouldReturnNullForNullInput() {
        assertNull(SanitizeUtils.escapeHtml(null));
    }

    @Test
    void escapeHtml_shouldReturnEmptyForEmptyInput() {
        assertEquals("", SanitizeUtils.escapeHtml(""));
    }

    @Test
    void escapeHtml_shouldEscapeAmpersand() {
        assertEquals("a&amp;b", SanitizeUtils.escapeHtml("a&b"));
    }

    @Test
    void escapeHtml_shouldEscapeLessThan() {
        assertEquals("a&lt;b", SanitizeUtils.escapeHtml("a<b"));
    }

    @Test
    void escapeHtml_shouldEscapeGreaterThan() {
        assertEquals("a&gt;b", SanitizeUtils.escapeHtml("a>b"));
    }

    @Test
    void escapeHtml_shouldEscapeDoubleQuote() {
        assertEquals("a&quot;b", SanitizeUtils.escapeHtml("a\"b"));
    }

    @Test
    void escapeHtml_shouldEscapeSingleQuote() {
        assertEquals("a&#39;b", SanitizeUtils.escapeHtml("a'b"));
    }

    @Test
    void escapeHtml_shouldEscapeMultipleCharacters() {
        assertEquals("&lt;a&amp;b&gt;", SanitizeUtils.escapeHtml("<a&b>"));
    }

    @Test
    void escapeHtml_shouldReturnPlainTextUnchanged() {
        assertEquals("hello world 你好", SanitizeUtils.escapeHtml("hello world 你好"));
    }

    @Test
    void sanitizeCsvCell_shouldReturnNullForNullInput() {
        assertNull(SanitizeUtils.sanitizeCsvCell(null));
    }

    @Test
    void sanitizeCsvCell_shouldReturnEmptyForEmptyInput() {
        assertEquals("", SanitizeUtils.sanitizeCsvCell(""));
    }

    @Test
    void sanitizeCsvCell_shouldPrefixEqualsSign() {
        assertEquals("'=SUM(A1:A10)", SanitizeUtils.sanitizeCsvCell("=SUM(A1:A10)"));
    }

    @Test
    void sanitizeCsvCell_shouldPrefixPlusSign() {
        assertEquals("'+100", SanitizeUtils.sanitizeCsvCell("+100"));
    }

    @Test
    void sanitizeCsvCell_shouldPrefixMinusSign() {
        assertEquals("'-100", SanitizeUtils.sanitizeCsvCell("-100"));
    }

    @Test
    void sanitizeCsvCell_shouldPrefixAtSign() {
        assertEquals("'@foo", SanitizeUtils.sanitizeCsvCell("@foo"));
    }

    @Test
    void sanitizeCsvCell_shouldNotPrefixNormalText() {
        assertEquals("hello", SanitizeUtils.sanitizeCsvCell("hello"));
    }
}
