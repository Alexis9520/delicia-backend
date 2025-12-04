package com.delicia.deliciabackend.util;

import com.delicia.deliciabackend.dto.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.util.List;

public class PaginationUtil {

    /**
     * Map a Spring Page<T> to PaginatedResponse<T> (stable JSON structure).
     * pageNumber must be 1-based for the DTO.
     */
    public static <T> PaginatedResponse<T> fromPage(Page<T> page, int pageNumber1Based, int pageSize) {
        List<T> content = page.getContent();
        long totalElements = page.getTotalElements();
        int totalPages = page.getTotalPages();
        return new PaginatedResponse<>(content, totalElements, pageNumber1Based, pageSize, totalPages);
    }

    /**
     * Map a Slice<T> to a PaginatedResponse<T>. Slice does not provide totalElements or totalPages
     * (avoids COUNT query). We set totalElements = content.size() and totalPages = -1 to indicate unknown.
     * If you need hasNext, you can check slice.hasNext() at the client side or extend DTO with hasNext.
     */
    public static <T> PaginatedResponse<T> fromSlice(Slice<T> slice, int pageNumber1Based, int pageSize) {
        List<T> content = slice.getContent();
        long totalElements = content.size(); // approximate only: Slice no tiene total global
        int totalPages = slice.hasNext() ? -1 : pageNumber1Based; // -1 indica "desconocido / no calculado"
        return new PaginatedResponse<>(content, totalElements, pageNumber1Based, pageSize, totalPages);
    }
}