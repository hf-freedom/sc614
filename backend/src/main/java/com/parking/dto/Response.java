package com.parking.dto;

import lombok.Data;

@Data
public class Response<T> {
    private int code;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.code = 200;
        response.message = "success";
        response.data = data;
        return response;
    }

    public static <T> Response<T> success() {
        return success(null);
    }

    public static <T> Response<T> error(String message) {
        Response<T> response = new Response<>();
        response.code = 500;
        response.message = message;
        return response;
    }

    public static <T> Response<T> error(int code, String message) {
        Response<T> response = new Response<>();
        response.code = code;
        response.message = message;
        return response;
    }
}
