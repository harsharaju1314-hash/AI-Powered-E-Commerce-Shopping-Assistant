package com.ecommerce.assistant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PageResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;

    public PageResponse() {}

    public PageResponse(List<T> content, int pageNumber, int pageSize, long totalElements, int totalPages, boolean isLast) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.isLast = isLast;
    }

    public List<T> getContent() { return content; }
    public void setContent(List<T> content) { this.content = content; }

    @JsonProperty("items")
    public List<T> getItems() { return content; }

    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }

    @JsonProperty("page")
    public int getPage() { return pageNumber; }

    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }

    @JsonProperty("page_size")
    public int getPageSizeSnake() { return pageSize; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    @JsonProperty("total")
    public long getTotal() { return totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }

    @JsonProperty("total_pages")
    public int getTotalPagesSnake() { return totalPages; }

    public boolean isLast() { return isLast; }
    public void setLast(boolean last) { isLast = last; }
}
