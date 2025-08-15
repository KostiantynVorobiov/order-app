package com.testScenario.model;

public record AppResponse<T>(
        boolean success,
        T data,
        String message
) {

    public static <T> AppResponse<T> ok(T data, String message) {
        return new AppResponse<>(true, data, message);
    }

    public static <T> AppResponse<T> fail(String message) {
        return new AppResponse<>(false, null, message);
    }
}
