package com.mentor.shared.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class PaginationQuery {

    @Min(1)
    private int page = 1;

    @Min(1)
    @Max(100)
    private int size = 10;

    private String keyword;
    private String category;

    // Never pass this straight into a query: check it against a whitelist of sortable fields
    private String sortBy;

    /** Spring Data pages are 0-based while the API is 1-based. */
    public int zeroBasedPage() {
        return page - 1;
    }
}
