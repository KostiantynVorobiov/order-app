package com.orderapp.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Generic API response wrapper")
public record AppResponse<T>(
        @Schema(description = "Success status of the operation", example = "true")
        boolean success,

        @Schema(description = "Returned data (depends on request)", nullable = true)
        T data,

        @Schema(description = "Response message", example = "Operation successful")
        String message
) {

    public static <T> AppResponse<T> ok(T data, String message) {
        return new AppResponse<>(true, data, message);
    }

    public static <T> AppResponse<T> fail(String message) {
        return new AppResponse<>(false, null, message);
    }
}
