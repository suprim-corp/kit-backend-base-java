package dev.suprim.kit.core;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataUtilsTest {

    // isNull tests
    @Test
    void isNull_shouldReturnTrueForNull() {
        assertTrue(DataUtils.isNull(null));
    }

    @Test
    void isNull_shouldReturnFalseForNonNull() {
        assertFalse(DataUtils.isNull("test"));
    }

    // nonNull tests
    @Test
    void nonNull_shouldReturnFalseForNull() {
        assertFalse(DataUtils.nonNull(null));
    }

    @Test
    void nonNull_shouldReturnTrueForNonNull() {
        assertTrue(DataUtils.nonNull("test"));
    }

    // isNullOrEmpty (String) tests
    @Test
    void isNullOrEmpty_String_shouldReturnTrueForNull() {
        assertTrue(DataUtils.isNullOrEmpty((String) null));
    }

    @Test
    void isNullOrEmpty_String_shouldReturnTrueForEmpty() {
        assertTrue(DataUtils.isNullOrEmpty(""));
    }

    @Test
    void isNullOrEmpty_String_shouldReturnFalseForNonEmpty() {
        assertFalse(DataUtils.isNullOrEmpty("test"));
    }

    @Test
    void isNullOrEmpty_String_shouldReturnFalseForBlank() {
        assertFalse(DataUtils.isNullOrEmpty("   "));
    }

    // isNullOrBlank tests
    @Test
    void isNullOrBlank_shouldReturnTrueForNull() {
        assertTrue(DataUtils.isNullOrBlank(null));
    }

    @Test
    void isNullOrBlank_shouldReturnTrueForEmpty() {
        assertTrue(DataUtils.isNullOrBlank(""));
    }

    @Test
    void isNullOrBlank_shouldReturnTrueForBlank() {
        assertTrue(DataUtils.isNullOrBlank("   "));
    }

    @Test
    void isNullOrBlank_shouldReturnFalseForNonBlank() {
        assertFalse(DataUtils.isNullOrBlank("test"));
    }

    // isNullOrEmpty (Collection) tests
    @Test
    void isNullOrEmpty_Collection_shouldReturnTrueForNull() {
        assertTrue(DataUtils.isNullOrEmpty((List<?>) null));
    }

    @Test
    void isNullOrEmpty_Collection_shouldReturnTrueForEmpty() {
        assertTrue(DataUtils.isNullOrEmpty(Collections.emptyList()));
    }

    @Test
    void isNullOrEmpty_Collection_shouldReturnFalseForNonEmpty() {
        assertFalse(DataUtils.isNullOrEmpty(List.of("item")));
    }

    // coalesce tests
    @Test
    void coalesce_shouldReturnFirstNonNull() {
        assertEquals("first", DataUtils.coalesce(null, "first", "second"));
    }

    @Test
    void coalesce_shouldReturnNullIfAllNull() {
        assertNull(DataUtils.coalesce(null, null, null));
    }

    @Test
    void coalesce_shouldReturnFirstIfNotNull() {
        assertEquals("first", DataUtils.coalesce("first", "second", "third"));
    }

    @Test
    void coalesce_shouldReturnNullForNullArray() {
        assertNull(DataUtils.coalesce((Object[]) null));
    }

    @Test
    void coalesce_shouldReturnNullForEmptyArray() {
        assertNull(DataUtils.coalesce());
    }

    // defaultIfBlank tests
    @Test
    void defaultIfBlank_shouldReturnDefaultForNull() {
        assertEquals("default", DataUtils.defaultIfBlank(null, "default"));
    }

    @Test
    void defaultIfBlank_shouldReturnDefaultForEmpty() {
        assertEquals("default", DataUtils.defaultIfBlank("", "default"));
    }

    @Test
    void defaultIfBlank_shouldReturnDefaultForBlank() {
        assertEquals("default", DataUtils.defaultIfBlank("  ", "default"));
    }

    @Test
    void defaultIfBlank_shouldReturnValueIfNotBlank() {
        assertEquals("value", DataUtils.defaultIfBlank("value", "default"));
    }

    // removeAccents tests
    @Test
    void removeAccents_shouldRemoveVietnameseAccents() {
        assertEquals("Nguyen Van An", DataUtils.removeAccents("Nguyễn Văn An"));
    }

    @Test
    void removeAccents_shouldHandleDWithStroke() {
        assertEquals("do", DataUtils.removeAccents("đô"));
    }

    @Test
    void removeAccents_shouldHandleUppercaseDWithStroke() {
        assertEquals("Do", DataUtils.removeAccents("Đô"));
    }

    @Test
    void removeAccents_shouldHandleNull() {
        assertNull(DataUtils.removeAccents(null));
    }

    @Test
    void removeAccents_shouldHandleEmpty() {
        assertEquals("", DataUtils.removeAccents(""));
    }

    @Test
    void removeAccents_shouldNormalizeSpaces() {
        assertEquals("hello world", DataUtils.removeAccents("  hello   world  "));
    }

    // toLowerCase tests
    @Test
    void toLowerCase_shouldConvert() {
        assertEquals("test", DataUtils.toLowerCase("TEST"));
    }

    @Test
    void toLowerCase_shouldHandleNull() {
        assertNull(DataUtils.toLowerCase(null));
    }

    // toUpperCase tests
    @Test
    void toUpperCase_shouldConvert() {
        assertEquals("TEST", DataUtils.toUpperCase("test"));
    }

    @Test
    void toUpperCase_shouldHandleNull() {
        assertNull(DataUtils.toUpperCase(null));
    }

    // trim tests
    @Test
    void trim_shouldRemoveWhitespace() {
        assertEquals("test", DataUtils.trim("  test  "));
    }

    @Test
    void trim_shouldHandleNull() {
        assertNull(DataUtils.trim(null));
    }

    // parseIntOrNull tests
    @Test
    void parseIntOrNull_shouldParseValidInteger() {
        assertEquals(123, DataUtils.parseIntOrNull("123"));
    }

    @Test
    void parseIntOrNull_shouldParseNegativeInteger() {
        assertEquals(-123, DataUtils.parseIntOrNull("-123"));
    }

    @Test
    void parseIntOrNull_shouldTrimWhitespace() {
        assertEquals(123, DataUtils.parseIntOrNull("  123  "));
    }

    @Test
    void parseIntOrNull_shouldReturnNullForInvalid() {
        assertNull(DataUtils.parseIntOrNull("abc"));
    }

    @Test
    void parseIntOrNull_shouldReturnNullForNull() {
        assertNull(DataUtils.parseIntOrNull(null));
    }

    @Test
    void parseIntOrNull_shouldReturnNullForBlank() {
        assertNull(DataUtils.parseIntOrNull("   "));
    }

    // parseLongOrNull tests
    @Test
    void parseLongOrNull_shouldParseValidLong() {
        assertEquals(123456789L, DataUtils.parseLongOrNull("123456789"));
    }

    @Test
    void parseLongOrNull_shouldReturnNullForInvalid() {
        assertNull(DataUtils.parseLongOrNull("abc"));
    }

    @Test
    void parseLongOrNull_shouldReturnNullForNull() {
        assertNull(DataUtils.parseLongOrNull(null));
    }

    @Test
    void parseLongOrNull_shouldReturnNullForBlank() {
        assertNull(DataUtils.parseLongOrNull("   "));
    }

    // parseDoubleOrNull tests
    @Test
    void parseDoubleOrNull_shouldParseValidDouble() {
        assertEquals(123.45, DataUtils.parseDoubleOrNull("123.45"));
    }

    @Test
    void parseDoubleOrNull_shouldReturnNullForInvalid() {
        assertNull(DataUtils.parseDoubleOrNull("abc"));
    }

    @Test
    void parseDoubleOrNull_shouldReturnNullForNull() {
        assertNull(DataUtils.parseDoubleOrNull(null));
    }

    @Test
    void parseDoubleOrNull_shouldReturnNullForBlank() {
        assertNull(DataUtils.parseDoubleOrNull("   "));
    }

    // parseBooleanOrNull tests
    @Test
    void parseBooleanOrNull_shouldReturnTrueForTrueValues() {
        assertTrue(DataUtils.parseBooleanOrNull("true"));
        assertTrue(DataUtils.parseBooleanOrNull("TRUE"));
        assertTrue(DataUtils.parseBooleanOrNull("1"));
        assertTrue(DataUtils.parseBooleanOrNull("yes"));
        assertTrue(DataUtils.parseBooleanOrNull("YES"));
        assertTrue(DataUtils.parseBooleanOrNull("on"));
        assertTrue(DataUtils.parseBooleanOrNull("ON"));
    }

    @Test
    void parseBooleanOrNull_shouldReturnFalseForOtherValues() {
        assertFalse(DataUtils.parseBooleanOrNull("false"));
        assertFalse(DataUtils.parseBooleanOrNull("0"));
        assertFalse(DataUtils.parseBooleanOrNull("no"));
        assertFalse(DataUtils.parseBooleanOrNull("off"));
        assertFalse(DataUtils.parseBooleanOrNull("random"));
    }

    @Test
    void parseBooleanOrNull_shouldReturnNullForNull() {
        assertNull(DataUtils.parseBooleanOrNull(null));
    }

    @Test
    void parseBooleanOrNull_shouldReturnNullForBlank() {
        assertNull(DataUtils.parseBooleanOrNull("   "));
    }

    // Private constructor test
    @Test
    void privateConstructor_shouldThrowException() throws Exception {
        var constructor = DataUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
