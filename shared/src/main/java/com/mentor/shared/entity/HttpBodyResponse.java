package com.mentor.shared.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpBodyResponse <T> {

    public static final String DEFAULT_MESSAGE = "Successful";

    private Integer status;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    @Builder.Default
    private String message = DEFAULT_MESSAGE;

    @JsonInclude(JsonInclude.Include.ALWAYS)
    private T data;

    private HttpBodyPagingResponse paging;

    private HttpBodyErrorResponse error;
}
