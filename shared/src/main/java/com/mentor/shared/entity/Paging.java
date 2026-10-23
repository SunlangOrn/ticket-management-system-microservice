package com.mentor.shared.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Builder
public class Paging <T> {

    private List<T> items;
    private int size;
    private int page;
    private long total;
    private int totalPages;
}
