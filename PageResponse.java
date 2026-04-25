package com.securetask.taskmanager.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {

    private List<T> content;        // the actual data for this page
    private int currentPage;        // which page you are on — starts at 0
    private int totalPages;         // how many pages exist in total
    private long totalElements;     // total number of records across all pages
    private boolean hasNext;        // is there a page after this one
    private boolean hasPrevious;    // is there a page before this one
    private int pageSize;           // how many records per page
}