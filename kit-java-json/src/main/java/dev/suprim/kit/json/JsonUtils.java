package dev.suprim.kit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JSON utilities for serialization, deserialization, and map conversions.
 */
public final class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                String dateString = p.getText();
                if (dateString == null || dateString.isEmpty()) {
                    return null;
                }
                try {
                    OffsetDateTime odt = OffsetDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    return odt.toLocalDateTime();
                } catch (Exception e1) {
                    try {
                        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (Exception e2) {
                        throw new IOException("Unable to parse date: " + dateString, e2);
                    }
                }
            }
        });

        OBJECT_MAPPER.registerModule(javaTimeModule);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Returns the shared ObjectMapper instance.
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * Serializes an object to JSON string.
     */
    public static String toJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object to JSON", e);
        }
    }

    /**
     * Deserializes JSON string to object.
     */
    public static <T> T fromJson(String json, Class<T> valueType) {
        try {
            return OBJECT_MAPPER.readValue(json, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Deserializes JSON string to object with TypeReference.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(json, typeRef);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize JSON to object", e);
        }
    }

    /**
     * Converts an object to Map using snake_case keys.
     */
    public static Map<String, Object> toMap(Object object) {
        try {
            return OBJECT_MAPPER.convertValue(object, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to map", e);
        }
    }

    /**
     * Converts an object to Map using specified naming strategy.
     */
    public static Map<String, Object> toMap(Object object, PropertyNamingStrategy namingStrategy) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.setPropertyNamingStrategy(namingStrategy != null
                ? namingStrategy
                : PropertyNamingStrategies.SNAKE_CASE);
            return mapper.convertValue(object, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to map", e);
        }
    }

    /**
     * Converts a Map to object.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromMap(Map<String, Object> source, Class<T> valueType) {
        try {
            Map<String, Object> normalized = (Map<String, Object>) normalizeJsonStrings(source);
            return OBJECT_MAPPER.convertValue(normalized, valueType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map to " + valueType.getSimpleName(), e);
        }
    }

    /**
     * Converts a List of Maps to List of objects.
     */
    public static <T> List<T> fromList(List<Map<String, Object>> source, Class<T> valueType) {
        try {
            return source.stream()
                .map(map -> OBJECT_MAPPER.convertValue(map, valueType))
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to map list to " + valueType.getSimpleName(), e);
        }
    }

    /**
     * Parses JSON string to Map.
     */
    public static Map<String, Object> parseJsonToMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON string to map: " + e.getMessage(), e);
        }
    }

    /**
     * Parses JSON string to Map with String values.
     */
    public static Map<String, String> parseJsonToStringMap(String json) {
        try {
            Map<String, Object> raw = OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
            Map<String, String> result = new HashMap<>();
            raw.forEach((k, v) -> result.put(k, v != null ? String.valueOf(v) : null));
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON to string map", e);
        }
    }

    /**
     * Safely get a List from a map.
     */
    public static List<?> getListFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof List<?> list ? list : List.of();
    }

    /**
     * Safely get a List from a map with default value.
     */
    public static List<?> getListFromMap(Map<String, Object> map, String key, List<?> defaultValue) {
        Object value = map.get(key);
        return value instanceof List<?> list ? list : defaultValue;
    }

    /**
     * Safely get a Map from a map.
     */
    public static Map<String, Object> getMapFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value instanceof Map<?, ?> rawMap ? convertToStringKeyMap(rawMap) : Map.of();
    }

    /**
     * Safely get a Map from a map with default value.
     */
    public static Map<String, Object> getMapFromMap(Map<String, Object> map, String key, Map<String, Object> defaultValue) {
        Object value = map.get(key);
        return value instanceof Map<?, ?> rawMap ? convertToStringKeyMap(rawMap) : defaultValue;
    }

    /**
     * Converts any object to List of Strings.
     */
    public static List<String> toStringList(Object obj) {
        if (obj instanceof List<?> list) {
            return list.stream().map(String::valueOf).collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Converts Map with any keys to Map with String keys.
     */
    public static Map<String, Object> toStringKeyMap(Object obj) {
        return obj instanceof Map<?, ?> rawMap ? convertToStringKeyMap(rawMap) : Map.of();
    }

    /**
     * Creates a mutable HashMap from key-value pair.
     */
    public static Map<String, Object> mutableMap(String key, Object value) {
        return new HashMap<>(Map.of(key, value));
    }

    /**
     * Parses any object (including PGobject) to Map.
     * Returns null if input is null.
     */
    public static Map<String, Object> parseToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Object normalized = normalizeJsonStrings(obj);
        if (normalized instanceof Map<?, ?> map) {
            return convertToStringKeyMap(map);
        }
        return null;
    }

    // Helper method to normalize JSON strings
    private static Object normalizeJsonStrings(Object node) {
        // Handle PostgreSQL PGobject via reflection to avoid compile-time dependency
        if (node != null && "org.postgresql.util.PGobject".equals(node.getClass().getName())) {
            try {
                java.lang.reflect.Method getValue = node.getClass().getMethod("getValue");
                String value = (String) getValue.invoke(node);
                return tryParseJson(value);
            } catch (Exception e) {
                return node;
            }
        }

        if (node instanceof Map<?, ?> m) {
            Map<Object, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(e.getKey(), normalizeJsonStrings(e.getValue()));
            }
            return out;
        }

        if (node instanceof List<?> list) {
            List<Object> out = new ArrayList<>(list.size());
            for (Object v : list) {
                out.add(normalizeJsonStrings(v));
            }
            return out;
        }

        if (node instanceof String s) {
            Object parsed = tryParseJson(s);
            if (parsed instanceof String) {
                return s;
            }
            return normalizeJsonStrings(parsed);
        }

        return node;
    }

    private static Object tryParseJson(String s) {
        if (s == null) return null;

        String cur = s.trim();
        // Bounded loop to prevent infinite recursion with deeply nested JSON strings
        for (int i = 0; i < 10; i++) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(cur);
                if (node.isTextual()) {
                    String inner = node.textValue();
                    cur = inner.trim();
                    continue;
                }
                return OBJECT_MAPPER.convertValue(node, Object.class);
            } catch (Exception ignore) {
                return cur;
            }
        }
        return cur;
    }

    private static Map<String, Object> convertToStringKeyMap(Map<?, ?> rawMap) {
        Map<String, Object> result = new HashMap<>();
        rawMap.forEach((k, v) -> result.put(String.valueOf(k), v));
        return result;
    }
}
