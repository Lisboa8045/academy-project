package com.academy.controllers;

import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.InvalidArgumentException;
import com.academy.exceptions.EntityAlreadyExists;
import com.academy.exceptions.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(InvalidArgumentException.class)
    public ResponseEntity<Object> handleInvalidValue(InvalidArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(EntityAlreadyExists.class)
    public ResponseEntity<Object> handleInvalidValue(EntityAlreadyExists e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleInvalidValue(AuthenticationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleInvalidValue(NotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }
}
