package dev.suprim.kit.web.response;

import dev.suprim.kit.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaginatedResponseTest {

    @Test
    void defaultConstructor_setsDefaults() {
        PaginatedResponse<String> response = new PaginatedResponse<>();

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(ApiStatus.SUCCESS.getMessage(), response.message());
        assertTrue(response.data().isEmpty());
        assertNotNull(response.pagination());
        assertEquals(0, response.pagination().total());
        assertTrue(response.pagination().empty());
    }

    @Test
    void dataAndTotalConstructor_setsBasicPagination() {
        List<String> data = List.of("a", "b", "c");
        PaginatedResponse<String> response = new PaginatedResponse<>(data, 100);

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(3, response.data().size());
        assertEquals(100, response.pagination().total());
    }

    @Test
    void pageableConstructor_calculatesPagination() {
        List<String> data = List.of("item1", "item2");
        Pageable pageable = PageRequest.of(0, 10);

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 25, pageable);

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals(2, response.data().size());
        assertEquals(25, response.pagination().total());
        assertEquals(3, response.pagination().totalPages());
        assertEquals(10, response.pagination().size());
        assertTrue(response.pagination().first());
        assertFalse(response.pagination().last());
    }

    @Test
    void pageableConstructor_detectsLastPage() {
        List<String> data = List.of("item1");
        // Use pageNumber=3 to get page >= totalPages (3 >= 3 = true for isLast)
        Pageable pageable = PageRequest.of(3, 10);

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 25, pageable);

        assertTrue(response.pagination().last());
        assertFalse(response.pagination().first());
        assertEquals(3, response.pagination().page());
    }

    @Test
    void pageableConstructor_handlesEmptyResult() {
        List<String> data = List.of();
        Pageable pageable = PageRequest.of(0, 10);

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 0, pageable);

        assertTrue(response.pagination().empty());
        assertEquals(0, response.pagination().total());
    }

    @Test
    void pageableConstructor_handlesSorting() {
        List<String> data = List.of("a");
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "name"));

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 10, pageable);

        assertFalse(response.pagination().sorting().isEmpty());
        assertEquals("name;DESC", response.pagination().sorting().get(0));
    }

    @Test
    void fullConstructor_setsAllFields() {
        List<Integer> data = List.of(1, 2, 3);
        Pageable pageable = PageRequest.of(1, 5);

        PaginatedResponse<Integer> response = new PaginatedResponse<>(
                ApiStatus.SUCCESS,
                "Custom message",
                data,
                50,
                pageable
        );

        assertEquals(ApiStatus.SUCCESS.getCode(), response.code());
        assertEquals("Custom message", response.message());
        assertEquals(3, response.data().size());
        assertEquals(50, response.pagination().total());
        assertEquals(10, response.pagination().totalPages());
    }

    @Test
    void pagination_handlesSmallPageSize() {
        List<String> data = List.of("a");
        Pageable pageable = PageRequest.of(0, 1);

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 5, pageable);

        assertEquals(5, response.pagination().totalPages());
        assertEquals(1, response.pagination().size());
    }

    @Test
    void pagination_handlesZeroPageSize_usesDefault() {
        List<String> data = List.of("a", "b");
        Pageable pageable = mock(Pageable.class);
        when(pageable.getPageSize()).thenReturn(0);
        when(pageable.getPageNumber()).thenReturn(1);
        when(pageable.getSort()).thenReturn(Sort.unsorted());

        PaginatedResponse<String> response = new PaginatedResponse<>(data, 40, pageable);

        assertEquals(20, response.pagination().size());
        assertEquals(2, response.pagination().totalPages());
    }

}
