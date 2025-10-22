package com.delicia.deliciabackend.dto;

import java.util.List;

public class PaginatedResponse<T> {
    private List<T> data;
    private int totalPages;
    private int currentPage;
    private int pageSize;
    private int totalElements; // NUEVO: total de elementos

    public PaginatedResponse() {}

    // Constructor mejorado para incluir total de elementos
    public PaginatedResponse(List<T> data, int totalElements, int currentPage, int pageSize, int totalPages) {
        this.data = data;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    // Constructor legacy (si solo usas totalPages)
    public PaginatedResponse(List<T> data, int totalPages, int currentPage, int pageSize) {
        this.data = data;
        this.totalPages = totalPages;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
}