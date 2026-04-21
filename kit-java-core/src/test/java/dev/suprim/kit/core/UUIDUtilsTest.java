package dev.suprim.kit.core;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UUIDUtilsTest {

    @Test
    void v4_shouldGenerateValidUUID() {
        UUID uuid = UUIDUtils.v4();
        assertNotNull(uuid);
        assertEquals(4, uuid.version());
    }

    @Test
    void v4_shouldGenerateUniqueUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            assertTrue(uuids.add(UUIDUtils.v4()), "UUID collision detected");
        }
    }

    @Test
    void v7_shouldGenerateValidUUID() {
        UUID uuid = UUIDUtils.v7();
        assertNotNull(uuid);
        assertEquals(7, uuid.version());
    }

    @Test
    void v7_shouldBeTimeOrdered() {
        UUID first = UUIDUtils.v7();
        UUID second = UUIDUtils.v7();

        // v7 UUIDs should be sortable by creation time
        assertTrue(first.compareTo(second) <= 0, "UUID v7 should be time-ordered");
    }

    // Private constructor test
    @Test
    void privateConstructor_shouldThrowException() throws Exception {
        var constructor = UUIDUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        assertThrows(java.lang.reflect.InvocationTargetException.class, constructor::newInstance);
    }
}
