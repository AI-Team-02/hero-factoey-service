//package com.gameservice.create_game_resource_service.exception;
//
//import org.springframework.http.HttpStatus;
//
//public class SupabaseException extends RuntimeException {
//    private final HttpStatus statusCode;
//
//    public SupabaseException(String message, HttpStatus statusCode) {
//        super(message);
//        this.statusCode = statusCode;
//    }
//
//    public SupabaseException(String message, HttpStatus statusCode, Throwable cause) {
//        super(message, cause);
//        this.statusCode = statusCode;
//    }
//
//    public HttpStatus getStatusCode() {
//        return statusCode;
//    }
//}