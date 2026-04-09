package dev.suprim.kit.web.response;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortUtilsTest {

    @Test
    void fromPageable_nullPageable_returnsEmptyList() {
        List<String> result = SortUtils.fromPageable(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void fromPageable_unsorted_returnsEmptyList() {
        Pageable pageable = PageRequest.of(0, 10);
        List<String> result = SortUtils.fromPageable(pageable);
        assertTrue(result.isEmpty());
    }

    @Test
    void fromPageable_singleSort_returnsFormattedString() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        List<String> result = SortUtils.fromPageable(pageable);

        assertEquals(1, result.size());
        assertEquals("name;ASC", result.get(0));
    }

    @Test
    void fromPageable_descendingSort_returnsFormattedString() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<String> result = SortUtils.fromPageable(pageable);

        assertEquals(1, result.size());
        assertEquals("createdAt;DESC", result.get(0));
    }

    @Test
    void fromPageable_multipleSort_returnsAllFormatted() {
        Sort sort = Sort.by(
                Sort.Order.asc("name"),
                Sort.Order.desc("createdAt")
        );
        Pageable pageable = PageRequest.of(0, 10, sort);
        List<String> result = SortUtils.fromPageable(pageable);

        assertEquals(2, result.size());
        assertEquals("name;ASC", result.get(0));
        assertEquals("createdAt;DESC", result.get(1));
    }

    @Test
    void constructor_throwsUnsupportedOperationException() throws Exception {
        Constructor<SortUtils> constructor = SortUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(ex.getCause() instanceof UnsupportedOperationException);
    }
}
