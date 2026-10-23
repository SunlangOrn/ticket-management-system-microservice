package com.mentor.shared.api;


import com.mentor.shared.entity.HttpBodyPagingResponse;
import com.mentor.shared.entity.HttpBodyResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class ControllerHandler {

    private ControllerHandler() {
    }

    public static <T> ResponseEntity<@NonNull HttpBodyResponse<T>> responseSucceed(T data) {
        return build(HttpStatus.OK, data);
    }

    public static <T> ResponseEntity<@NonNull HttpBodyResponse<T>> responseCreated(T data) {
        return build(HttpStatus.CREATED, data);
    }

    public static ResponseEntity<@NonNull Void> responseDeleted() {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    public static <T> ResponseEntity<@NonNull HttpBodyResponse<T>> responsePaged(
            T data, HttpBodyPagingResponse pagingResponse) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(
                        HttpBodyResponse.<T>builder()
                                .status(HttpStatus.OK.value())
                                .message(HttpStatus.OK.getReasonPhrase())
                                .data(data)
                                .paging(pagingResponse)
                                .build());
    }

    private static <T> ResponseEntity<@NonNull HttpBodyResponse<T>> build(HttpStatus status, T data) {
        return ResponseEntity.status(status)
                .body(
                        HttpBodyResponse.<T>builder()
                                .status(status.value())
                                .message(status.getReasonPhrase())
                                .data(data)
                                .build());
    }
}
