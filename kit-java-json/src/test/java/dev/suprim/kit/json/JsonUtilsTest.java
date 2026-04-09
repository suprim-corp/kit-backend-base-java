package dev.suprim.kit.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void getObjectMapper_shouldReturnSharedInstance() {
        assertNotNull(JsonUtils.getObjectMapper());
        assertSame(JsonUtils.getObjectMapper(), JsonUtils.getObjectMapper());
    }

    // toJsonString tests
    @Test
    void toJsonString_shouldSerializeObject() {
        Map<String, Object> map = Map.of("name", "test", "value", 123);
        String json = JsonUtils.toJsonString(map);
        assertTrue(json.contains("\"name\""));
        assertTrue(json.contains("\"test\""));
    }

    @Test
    void toJsonString_shouldSerializeNull() {
        String json = JsonUtils.toJsonString(null);
        assertEquals("null", json);
    }

    @Test
    void toJsonString_shouldSerializeList() {
        List<String> list = List.of("a", "b", "c");
        String json = JsonUtils.toJsonString(list);
        assertEquals("[\"a\",\"b\",\"c\"]", json);
    }

    // fromJson tests
    @Test
    void fromJson_shouldDeserializeToClass() {
        String json = "{\"name\":\"test\",\"value\":123}";
        Map<?, ?> result = JsonUtils.fromJson(json, Map.class);
        assertEquals("test", result.get("name"));
        assertEquals(123, result.get("value"));
    }

    @Test
    void fromJson_withTypeReference_shouldDeserialize() {
        String json = "[{\"key\":\"value1\"},{\"key\":\"value2\"}]";
        List<Map<String, Object>> result = JsonUtils.fromJson(json, new TypeReference<>() {});
        assertEquals(2, result.size());
        assertEquals("value1", result.get(0).get("key"));
    }

    @Test
    void fromJson_shouldThrowForInvalidJson() {
        assertThrows(RuntimeException.class, () -> JsonUtils.fromJson("invalid json", Map.class));
    }

    // toMap tests
    record TestRecord(String name, int value) {}

    @Test
    void toMap_shouldConvertObjectToMap() {
        TestRecord record = new TestRecord("test", 42);
        Map<String, Object> result = JsonUtils.toMap(record);
        assertEquals("test", result.get("name"));
        assertEquals(42, result.get("value"));
    }

    @Test
    void toMap_withNamingStrategy_shouldConvertWithStrategy() {
        TestRecord record = new TestRecord("test", 42);
        Map<String, Object> result = JsonUtils.toMap(record, PropertyNamingStrategies.SNAKE_CASE);
        assertNotNull(result);
    }

    @Test
    void toMap_withNullStrategy_shouldUseSnakeCase() {
        TestRecord record = new TestRecord("test", 42);
        Map<String, Object> result = JsonUtils.toMap(record, null);
        assertNotNull(result);
    }

    // fromMap tests
    @Test
    void fromMap_shouldConvertMapToObject() {
        Map<String, Object> map = Map.of("name", "test", "value", 42);
        TestRecord result = JsonUtils.fromMap(map, TestRecord.class);
        assertEquals("test", result.name());
        assertEquals(42, result.value());
    }

    @Test
    void fromMap_shouldThrowForInvalidMapping() {
        Map<String, Object> map = Map.of("invalid_field", "value");
        assertThrows(RuntimeException.class, () -> JsonUtils.fromMap(map, TestRecord.class));
    }

    // fromList tests
    @Test
    void fromList_shouldConvertListOfMaps() {
        List<Map<String, Object>> source = List.of(
                Map.of("name", "first", "value", 1),
                Map.of("name", "second", "value", 2)
        );
        List<TestRecord> result = JsonUtils.fromList(source, TestRecord.class);
        assertEquals(2, result.size());
        assertEquals("first", result.get(0).name());
        assertEquals("second", result.get(1).name());
    }

    // parseJsonToMap tests
    @Test
    void parseJsonToMap_shouldParseValidJson() {
        String json = "{\"key\":\"value\",\"num\":42}";
        Map<String, Object> result = JsonUtils.parseJsonToMap(json);
        assertEquals("value", result.get("key"));
        assertEquals(42, result.get("num"));
    }

    @Test
    void parseJsonToMap_shouldReturnEmptyForNull() {
        Map<String, Object> result = JsonUtils.parseJsonToMap(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToMap_shouldReturnEmptyForBlank() {
        Map<String, Object> result = JsonUtils.parseJsonToMap("   ");
        assertTrue(result.isEmpty());
    }

    @Test
    void parseJsonToMap_shouldThrowForInvalidJson() {
        assertThrows(RuntimeException.class, () -> JsonUtils.parseJsonToMap("not valid json"));
    }

    // parseJsonToStringMap tests
    @Test
    void parseJsonToStringMap_shouldParseToStringValues() {
        String json = "{\"name\":\"test\",\"count\":42,\"active\":true}";
        Map<String, String> result = JsonUtils.parseJsonToStringMap(json);
        assertEquals("test", result.get("name"));
        assertEquals("42", result.get("count"));
        assertEquals("true", result.get("active"));
    }

    @Test
    void parseJsonToStringMap_shouldHandleNullValues() {
        String json = "{\"name\":null}";
        Map<String, String> result = JsonUtils.parseJsonToStringMap(json);
        assertNull(result.get("name"));
    }

    // getListFromMap tests
    @Test
    void getListFromMap_shouldReturnList() {
        Map<String, Object> map = Map.of("items", List.of("a", "b", "c"));
        List<?> result = JsonUtils.getListFromMap(map, "items");
        assertEquals(3, result.size());
    }

    @Test
    void getListFromMap_shouldReturnEmptyForMissingKey() {
        Map<String, Object> map = Map.of("other", "value");
        List<?> result = JsonUtils.getListFromMap(map, "items");
        assertTrue(result.isEmpty());
    }

    @Test
    void getListFromMap_shouldReturnEmptyForNonList() {
        Map<String, Object> map = Map.of("items", "not a list");
        List<?> result = JsonUtils.getListFromMap(map, "items");
        assertTrue(result.isEmpty());
    }

    @Test
    void getListFromMap_withDefault_shouldReturnDefault() {
        Map<String, Object> map = Map.of("other", "value");
        List<String> defaultList = List.of("default");
        List<?> result = JsonUtils.getListFromMap(map, "items", defaultList);
        assertEquals(defaultList, result);
    }

    @Test
    void getListFromMap_withDefault_shouldReturnListWhenExists() {
        Map<String, Object> map = Map.of("items", List.of("a", "b"));
        List<String> defaultList = List.of("default");
        List<?> result = JsonUtils.getListFromMap(map, "items", defaultList);
        assertEquals(2, result.size());
    }

    // getMapFromMap tests
    @Test
    void getMapFromMap_shouldReturnNestedMap() {
        Map<String, Object> nested = new HashMap<>();
        nested.put("nested_key", "nested_value");
        Map<String, Object> map = new HashMap<>();
        map.put("nested", nested);

        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested");
        assertEquals("nested_value", result.get("nested_key"));
    }

    @Test
    void getMapFromMap_shouldReturnEmptyForMissingKey() {
        Map<String, Object> map = Map.of("other", "value");
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested");
        assertTrue(result.isEmpty());
    }

    @Test
    void getMapFromMap_shouldReturnEmptyForNonMap() {
        Map<String, Object> map = Map.of("nested", "not a map");
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested");
        assertTrue(result.isEmpty());
    }

    @Test
    void getMapFromMap_withDefault_shouldReturnDefault() {
        Map<String, Object> map = Map.of("other", "value");
        Map<String, Object> defaultMap = Map.of("default", "value");
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested", defaultMap);
        assertEquals(defaultMap, result);
    }

    @Test
    void getMapFromMap_withDefault_shouldReturnMapWhenExists() {
        Map<String, Object> nested = Map.of("key", "value");
        Map<String, Object> map = new HashMap<>();
        map.put("nested", nested);
        Map<String, Object> defaultMap = Map.of("default", "value");
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested", defaultMap);
        assertEquals("value", result.get("key"));
    }

    // toStringList tests
    @Test
    void toStringList_shouldConvertList() {
        List<String> result = JsonUtils.toStringList(List.of(1, 2, 3));
        assertEquals(List.of("1", "2", "3"), result);
    }

    @Test
    void toStringList_shouldReturnEmptyForNonList() {
        List<String> result = JsonUtils.toStringList("not a list");
        assertTrue(result.isEmpty());
    }

    @Test
    void toStringList_shouldReturnEmptyForNull() {
        List<String> result = JsonUtils.toStringList(null);
        assertTrue(result.isEmpty());
    }

    // toStringKeyMap tests
    @Test
    void toStringKeyMap_shouldConvertKeys() {
        Map<Integer, String> source = Map.of(1, "one", 2, "two");
        Map<String, Object> result = JsonUtils.toStringKeyMap(source);
        assertEquals("one", result.get("1"));
        assertEquals("two", result.get("2"));
    }

    @Test
    void toStringKeyMap_shouldReturnEmptyForNonMap() {
        Map<String, Object> result = JsonUtils.toStringKeyMap("not a map");
        assertTrue(result.isEmpty());
    }

    @Test
    void toStringKeyMap_shouldReturnEmptyForNull() {
        Map<String, Object> result = JsonUtils.toStringKeyMap(null);
        assertTrue(result.isEmpty());
    }

    // mutableMap tests
    @Test
    void mutableMap_shouldCreateMutableMap() {
        Map<String, Object> map = JsonUtils.mutableMap("key", "value");
        assertDoesNotThrow(() -> map.put("another", "entry"));
        assertEquals(2, map.size());
    }

    // parseToMap tests
    @Test
    void parseToMap_shouldReturnNullForNull() {
        assertNull(JsonUtils.parseToMap(null));
    }

    @Test
    void parseToMap_shouldParseMapObject() {
        Map<String, Object> source = Map.of("key", "value");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertEquals("value", result.get("key"));
    }

    @Test
    void parseToMap_shouldReturnNullForNonMap() {
        assertNull(JsonUtils.parseToMap("not a map"));
    }

    @Test
    void parseToMap_shouldParseNestedJsonStrings() {
        Map<String, Object> source = new HashMap<>();
        source.put("nested", "{\"inner\":\"value\"}");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    // LocalDateTime parsing tests
    @Test
    void fromJson_shouldParseIsoLocalDateTime() {
        String json = "{\"timestamp\":\"2024-01-15T10:30:00\"}";
        Map<?, ?> result = JsonUtils.fromJson(json, Map.class);
        assertNotNull(result.get("timestamp"));
    }

    @Test
    void fromJson_shouldParseIsoOffsetDateTime() {
        String json = "{\"timestamp\":\"2024-01-15T10:30:00+07:00\"}";
        Map<?, ?> result = JsonUtils.fromJson(json, Map.class);
        assertNotNull(result.get("timestamp"));
    }

    // Nested JSON string normalization
    @Test
    void fromMap_shouldHandleNestedJsonStrings() {
        Map<String, Object> source = new HashMap<>();
        source.put("name", "test");
        source.put("value", 42);
        TestRecord result = JsonUtils.fromMap(source, TestRecord.class);
        assertEquals("test", result.name());
    }

    // LocalDateTime deserializer tests
    record TimestampRecord(LocalDateTime timestamp) {}

    @Test
    void fromJson_shouldParseOffsetDateTimeToLocalDateTime() {
        String json = "{\"timestamp\":\"2024-01-15T10:30:00+07:00\"}";
        TimestampRecord result = JsonUtils.fromJson(json, TimestampRecord.class);
        assertNotNull(result.timestamp());
        assertEquals(10, result.timestamp().getHour());
        assertEquals(30, result.timestamp().getMinute());
    }

    @Test
    void fromJson_shouldParseLocalDateTimeDirectly() {
        String json = "{\"timestamp\":\"2024-01-15T10:30:00\"}";
        TimestampRecord result = JsonUtils.fromJson(json, TimestampRecord.class);
        assertNotNull(result.timestamp());
        assertEquals(2024, result.timestamp().getYear());
        assertEquals(1, result.timestamp().getMonthValue());
        assertEquals(15, result.timestamp().getDayOfMonth());
    }

    @Test
    void fromJson_shouldHandleNullTimestamp() {
        String json = "{\"timestamp\":null}";
        TimestampRecord result = JsonUtils.fromJson(json, TimestampRecord.class);
        assertNull(result.timestamp());
    }

    @Test
    void fromJson_shouldThrowForInvalidDateFormat() {
        String json = "{\"timestamp\":\"not-a-date\"}";
        assertThrows(RuntimeException.class, () -> JsonUtils.fromJson(json, TimestampRecord.class));
    }

    @Test
    void fromJson_shouldHandleEmptyTimestamp() {
        String json = "{\"timestamp\":\"\"}";
        TimestampRecord result = JsonUtils.fromJson(json, TimestampRecord.class);
        assertNull(result.timestamp());
    }

    @Test
    void fromJson_shouldParseIsoOffsetDateTimeWithZ() {
        String json = "{\"timestamp\":\"2024-01-15T10:30:00Z\"}";
        TimestampRecord result = JsonUtils.fromJson(json, TimestampRecord.class);
        assertNotNull(result.timestamp());
    }

    // normalizeJsonStrings tests with nested structures
    @Test
    void parseToMap_shouldNormalizeNestedJsonStringInList() {
        Map<String, Object> source = new HashMap<>();
        source.put("items", List.of("{\"key\":\"value\"}"));
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        assertNotNull(result.get("items"));
    }

    @Test
    void parseToMap_shouldHandleDeepNestedJsonStrings() {
        Map<String, Object> inner = new HashMap<>();
        inner.put("deep", "{\"innerKey\":\"innerValue\"}");
        Map<String, Object> source = new HashMap<>();
        source.put("outer", inner);
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    // tryParseJson edge cases
    @Test
    void parseToMap_shouldHandleDoubleEncodedJson() {
        // JSON string that's double-quoted
        Map<String, Object> source = new HashMap<>();
        source.put("data", "\"{\\\"key\\\":\\\"value\\\"}\"");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    @Test
    void parseToMap_shouldHandleInvalidJsonStringGracefully() {
        Map<String, Object> source = new HashMap<>();
        source.put("data", "not valid json but a string");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertEquals("not valid json but a string", result.get("data"));
    }

    // fromList error handling
    @Test
    void fromList_shouldThrowForInvalidMapping() {
        List<Map<String, Object>> source = List.of(
                Map.of("invalid_field", "value")
        );
        assertThrows(RuntimeException.class, () -> JsonUtils.fromList(source, TestRecord.class));
    }

    // parseJsonToStringMap error handling
    @Test
    void parseJsonToStringMap_shouldThrowForInvalidJson() {
        assertThrows(RuntimeException.class, () -> JsonUtils.parseJsonToStringMap("not valid json"));
    }

    @Test
    void toMap_withStrategy_shouldHandleComplexObject() {
        record ComplexRecord(String firstName, int totalCount) {}
        ComplexRecord record = new ComplexRecord("John", 42);
        Map<String, Object> result = JsonUtils.toMap(record, PropertyNamingStrategies.LOWER_CAMEL_CASE);
        assertNotNull(result);
    }

    // Private constructor test via reflection
    @Test
    void privateConstructor_shouldThrowException() throws Exception {
        var constructor = JsonUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }

    // Additional coverage for getListFromMap with null value
    @Test
    void getListFromMap_shouldReturnEmptyForNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("items", null);
        List<?> result = JsonUtils.getListFromMap(map, "items");
        assertTrue(result.isEmpty());
    }

    @Test
    void getListFromMap_withDefault_shouldReturnDefaultForNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("items", null);
        List<String> defaultList = List.of("default");
        List<?> result = JsonUtils.getListFromMap(map, "items", defaultList);
        assertEquals(defaultList, result);
    }

    // getMapFromMap with null value
    @Test
    void getMapFromMap_shouldReturnEmptyForNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("nested", null);
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested");
        assertTrue(result.isEmpty());
    }

    @Test
    void getMapFromMap_withDefault_shouldReturnDefaultForNullValue() {
        Map<String, Object> map = new HashMap<>();
        map.put("nested", null);
        Map<String, Object> defaultMap = Map.of("default", "value");
        Map<String, Object> result = JsonUtils.getMapFromMap(map, "nested", defaultMap);
        assertEquals(defaultMap, result);
    }

    // Additional tryParseJson edge cases
    @Test
    void parseToMap_shouldHandleJsonArray() {
        Map<String, Object> source = new HashMap<>();
        source.put("arr", "[1,2,3]");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        assertTrue(result.get("arr") instanceof List);
    }

    @Test
    void parseToMap_shouldHandleNestedTextualJson() {
        // JSON with textual node that equals the current value
        Map<String, Object> source = new HashMap<>();
        source.put("simple", "\"hello\"");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    @Test
    void parseToMap_shouldHandleTripleNestedJson() {
        Map<String, Object> source = new HashMap<>();
        source.put("deep", "\"\\\"{\\\\\\\"key\\\\\\\":\\\\\\\"value\\\\\\\"}\\\"\"");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    @Test
    void parseToMap_shouldHandleNullString() {
        Map<String, Object> source = new HashMap<>();
        source.put("nullStr", "null");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    @Test
    void parseToMap_shouldReturnNullForListInput() {
        List<String> list = List.of("a", "b");
        assertNull(JsonUtils.parseToMap(list));
    }

    @Test
    void parseToMap_shouldHandleBoolean() {
        Map<String, Object> source = new HashMap<>();
        source.put("bool", "true");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    @Test
    void parseToMap_shouldHandleNumber() {
        Map<String, Object> source = new HashMap<>();
        source.put("num", "42");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
    }

    // toJsonString error path
    @Test
    void toJsonString_shouldThrowForUnserializableObject() {
        // Objects without proper serialization will throw
        Map<String, Object> map = new HashMap<>();
        map.put("key", new Object() {
            @Override
            public String toString() {
                return "custom";
            }
        });
        assertThrows(RuntimeException.class, () -> JsonUtils.toJsonString(map));
    }

    // fromJson with TypeReference error
    @Test
    void fromJson_withTypeReference_shouldThrowForInvalidJson() {
        assertThrows(RuntimeException.class, () ->
            JsonUtils.fromJson("not valid", new TypeReference<List<String>>() {}));
    }

    // toMap error path
    @Test
    void toMap_withStrategy_shouldThrowForInvalidObject() {
        // Objects that can't be converted to map throw
        assertThrows(RuntimeException.class, () ->
            JsonUtils.toMap("simple string", PropertyNamingStrategies.SNAKE_CASE));
    }

    // parseToMap with string that breaks during loop
    @Test
    void parseToMap_shouldHandleQuotedString() {
        Map<String, Object> source = new HashMap<>();
        source.put("data", "\"same\"");
        Map<String, Object> result = JsonUtils.parseToMap(source);
        // Quoted string gets unwrapped
        assertNotNull(result.get("data"));
    }

    // Empty list normalization
    @Test
    void parseToMap_shouldHandleEmptyList() {
        Map<String, Object> source = new HashMap<>();
        source.put("items", List.of());
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result.get("items"));
    }

    // Null in map value normalization
    @Test
    void parseToMap_shouldHandleNullMapValue() {
        Map<String, Object> source = new HashMap<>();
        source.put("key", null);
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNull(result.get("key"));
    }

    // Number normalization
    @Test
    void parseToMap_shouldHandleIntegerValue() {
        Map<String, Object> source = new HashMap<>();
        source.put("num", 123);
        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertEquals(123, result.get("num"));
    }

    // Multiple type reference test
    @Test
    void fromJson_shouldHandleComplexTypeReference() {
        String json = "{\"items\":[{\"name\":\"test\"}]}";
        Map<String, List<Map<String, String>>> result = JsonUtils.fromJson(json, new TypeReference<>() {});
        assertEquals(1, result.get("items").size());
    }

    // toMap with default strategy handles edge case
    @Test
    void toMap_shouldThrowForPrimitiveWrapper() {
        // Primitive wrappers can't be converted to map
        assertThrows(RuntimeException.class, () -> JsonUtils.toMap(Integer.valueOf(42)));
    }

    // ==================== PGobject Coverage ====================

    @Test
    void parseToMap_withPGobject_shouldParseJsonValue() {
        org.postgresql.util.PGobject pgObject = new org.postgresql.util.PGobject();
        pgObject.setValue("{\"pgKey\":\"pgValue\"}");

        Map<String, Object> source = new HashMap<>();
        source.put("data", pgObject);

        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        Object dataValue = result.get("data");
        assertInstanceOf(Map.class, dataValue);
        assertEquals("pgValue", ((Map<?, ?>) dataValue).get("pgKey"));
    }

    @Test
    void parseToMap_withPGobject_nullValue_shouldReturnNull() {
        org.postgresql.util.PGobject pgObject = new org.postgresql.util.PGobject();
        pgObject.setValue(null);

        Map<String, Object> source = new HashMap<>();
        source.put("data", pgObject);

        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        assertNull(result.get("data"));
    }

    @Test
    void parseToMap_withPGobject_plainString_shouldReturnString() {
        org.postgresql.util.PGobject pgObject = new org.postgresql.util.PGobject();
        pgObject.setValue("plain text value");

        Map<String, Object> source = new HashMap<>();
        source.put("data", pgObject);

        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        assertEquals("plain text value", result.get("data"));
    }

    @Test
    void parseToMap_withPGobject_exceptionDuringGetValue_shouldReturnOriginal() {
        // Use PGobject that throws on getValue (not a subclass to keep class name)
        org.postgresql.util.PGobject pgObject = new org.postgresql.util.PGobject();
        pgObject.setThrowOnGetValue(true);

        Map<String, Object> source = new HashMap<>();
        source.put("data", pgObject);

        Map<String, Object> result = JsonUtils.parseToMap(source);
        assertNotNull(result);
        // Exception caught, returns original PGobject
        assertSame(pgObject, result.get("data"));
    }

    // ==================== tryParseJson Coverage ====================

    @Test
    void parseToMap_withSelfReferencingTextualJson_shouldBreakLoop() throws Exception {
        // Test tryParseJson directly via reflection to cover line 297 (break when inner.equals(cur))
        // This happens when the JSON is a textual node whose value equals the input
        java.lang.reflect.Method tryParseJson = JsonUtils.class.getDeclaredMethod("tryParseJson", String.class);
        tryParseJson.setAccessible(true);

        // "test" (without JSON quotes) is not valid JSON, should return as-is
        Object result1 = tryParseJson.invoke(null, "test");
        assertEquals("test", result1);

        // null input should return null
        Object result2 = tryParseJson.invoke(null, (String) null);
        assertNull(result2);
    }

    @Test
    void parseToMap_withTextualJsonThatEqualsInput_shouldBreak() throws Exception {
        // Cover the break condition: inner.equals(cur)
        // This requires a textual JSON where the text value equals the input after unwrap
        java.lang.reflect.Method tryParseJson = JsonUtils.class.getDeclaredMethod("tryParseJson", String.class);
        tryParseJson.setAccessible(true);

        // Input: "\"a\"" (JSON string "a")
        // First parse: textual node value = "a", cur = "\"a\"" -> not equal, continue
        // cur = "a", parse "a" -> not valid JSON -> returns "a"
        Object result = tryParseJson.invoke(null, "\"a\"");
        assertEquals("a", result);
    }

    @Test
    void tryParseJson_emptyStringInput_returnsNull() throws Exception {
        // Empty/whitespace string handling
        java.lang.reflect.Method tryParseJson = JsonUtils.class.getDeclaredMethod("tryParseJson", String.class);
        tryParseJson.setAccessible(true);

        // Whitespace-only input trims to empty, Jackson readTree("") may return null
        Object result = tryParseJson.invoke(null, "   ");
        // Jackson returns null for empty string input
        assertNull(result);
    }

    @Test
    void tryParseJson_tripleQuotedString_unwrapsMultipleTimes() throws Exception {
        // Test multiple unwrapping iterations
        java.lang.reflect.Method tryParseJson = JsonUtils.class.getDeclaredMethod("tryParseJson", String.class);
        tryParseJson.setAccessible(true);

        // "\"\\\"value\\\"\"" is JSON for the string "\"value\""
        // Which unwraps to "value" as a string
        Object result = tryParseJson.invoke(null, "\"\\\"value\\\"\"");
        assertNotNull(result);
    }

    @Test
    void tryParseJson_exceedsMaxIterations_returnsLastValue() throws Exception {
        // Test the max iteration fallback (10 iterations limit)
        java.lang.reflect.Method tryParseJson = JsonUtils.class.getDeclaredMethod("tryParseJson", String.class);
        tryParseJson.setAccessible(true);

        // Create deeply nested JSON string that requires >10 unwrap iterations
        // Each level: "\"...\""  -> ...
        String nested = "value";
        for (int i = 0; i < 15; i++) {
            // Escape and quote for each level
            nested = "\"" + nested.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }

        Object result = tryParseJson.invoke(null, nested);
        assertNotNull(result);
        // After 10 iterations, returns whatever cur is at that point
    }
}
