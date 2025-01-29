package com.cloud.webapp.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
class CustomExceptionHandler {


//    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
//    public ResponseEntity<Void> handleMethodNotAllowed(@RequestBody(required = false) String body, HttpServletRequest request) {
//    	if (!request.getParameterMap().isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//        if (body != null && !body.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//        }
//        
//        System.out.println("controle is here in exception");
//    	return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).build();
//    }
//    
//    @ExceptionHandler
//    public ResponseEntity<Void> handleHttpMessageNotReadableException(Exception e) {
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
//    }
}
