package com.mentor.shared.entity;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpBodyPagingResponse {

    private int page;
    private int size;
    private long total;
    private long totalPages;

    public static HttpBodyPagingResponse from(Map<String, Object> source) {
        return HttpBodyPagingResponse.builder()
                .page(number(source, "page").intValue())
                .size(number(source, "size").intValue())
                .total(number(source, "total").longValue())
                .totalPages(number(source, "totalPages").longValue())
                .build();
    }

    private static Number number(Map<String, Object> source, String key) {
        return source.get(key) instanceof Number n ? n : 0;
    }
}