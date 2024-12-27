package com.reliaquest.api.controller;

import com.reliaquest.api.exceptions.EmployeeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class IEmployeeControllerAdvice {

    @ExceptionHandler(value = {EmployeeNotFoundException.class})
    public ResponseEntity<Object> handleExceptionCausingEmployeeNotFound(RuntimeException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(value = {IllegalArgumentException.class})
    public ResponseEntity<Object> handleExceptionCausingIllegalArgument(RuntimeException e){
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
