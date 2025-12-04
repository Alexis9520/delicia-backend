package com.delicia.deliciabackend.dto;

import java.util.List;

/**
 * DTO de respuesta paginada estable y explícita para la API.
 * Cambiado: el campo principal es 'data' para compatibilidad con el frontend.
 */
public class PaginatedResponse<T> {
    private List<T> data;         // <-- antes 'content', ahora 'data'
    private long totalElements;
    private int page; // 1-based
    private int pageSize;
    private int totalPages;
    private boolean hasNext; // útil para slice/load-more

    public PaginatedResponse() {}

    /**
     * Constructor clásico sin hasNext.
     */
    public PaginatedResponse(List<T> data, long totalElements, int page, int pageSize, int totalPages) {
        this.data = data;
        this.totalElements = totalElements;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.hasNext = false;
    }

    /**
     * Constructor completo con hasNext.
     */
    public PaginatedResponse(List<T> data, long totalElements, int page, int pageSize, int totalPages, boolean hasNext) {
        this.data = data;
        this.totalElements = totalElements;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.hasNext = hasNext;
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
}