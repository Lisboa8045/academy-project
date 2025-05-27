package com.academy.controllers;

import com.academy.exceptions.EntityNotFoundException;

import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ControllerAdvice;

import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Collections;

import java.util.HashMap;

import java.util.Map;

@ControllerAdvice

public class ExceptionController {

    @ExceptionHandler(MethodArgumentNotValidException.class)

    public ResponseEntity<Object> handleInvalidValue(MethodArgumentNotValidException e) {

        Map<String, String> errors = new HashMap<>();

        e.getBindingResult().getAllErrors().forEach((error) -> {

            String fieldName = ((FieldError) error).getField();

            String message = error.getDefaultMessage();

            errors.put(fieldName, message);

        });

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);

    }

    @ExceptionHandler(EntityNotFoundException.class)

    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException e) {

        String key = e.getEntityClass().getSimpleName();

        String value = "id " + e.getId() + " not found";

        Map<String, String> body = Collections.singletonMap(key, value);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);

    }
}

