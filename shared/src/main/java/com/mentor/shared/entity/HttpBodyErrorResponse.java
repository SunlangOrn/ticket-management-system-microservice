package com.mentor.shared.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HttpBodyErrorResponse {

    private String type;
    private String code;
    private String message;
    private String error;
    private Map<String, String> bodyRequestError;
}
