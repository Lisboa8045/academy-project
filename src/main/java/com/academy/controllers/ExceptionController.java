package com.academy.controllers;

import com.academy.exceptions.AuthenticationException;
import com.academy.exceptions.EmailTemplateLoadingException;
import com.academy.exceptions.EntityAlreadyExists;
import com.academy.exceptions.EntityNotFoundException;
import com.academy.exceptions.MemberNotFoundByEmailException;
import com.academy.exceptions.MemberNotFoundException;
import com.academy.exceptions.NotFoundException;
import com.academy.exceptions.RegistrationConflictException;
import com.academy.exceptions.SendEmailException;
import com.academy.exceptions.TokenExpiredException;
import com.academy.exceptions.UnavailableUserException;
import org.apache.coyote.BadRequestException;
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

    @ExceptionHandler(EntityAlreadyExists.class)
    public ResponseEntity<Object> handleInvalidValue(EntityAlreadyExists e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RegistrationConflictException.class)
    public ResponseEntity<Object> handleInvalidValue(RegistrationConflictException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(Map.of("errors", e.getFieldErrors()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleInvalidValue(BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }
        
    @ExceptionHandler(com.academy.exceptions.BadRequestException.class)
    public ResponseEntity<Object> handleInvalidValue(com.academy.exceptions.BadRequestException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(SendEmailException.class)
    public ResponseEntity<Object> handleInvalidValue(SendEmailException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleInvalidValue(TokenExpiredException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.GONE);
    }

    @ExceptionHandler(EmailTemplateLoadingException.class)
    public ResponseEntity<Object> handleInvalidValue(EmailTemplateLoadingException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleInvalidValue(AuthenticationException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleInvalidValue(NotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MemberNotFoundByEmailException.class)
    public ResponseEntity<Object> handleInvalidValue(MemberNotFoundByEmailException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<Object> handleInvalidValue(MemberNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnavailableUserException.class)
    public ResponseEntity<Object> handleInvalidValue(UnavailableUserException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EntityNotFoundException.class)

    public ResponseEntity<Object> handleEntityNotFound(EntityNotFoundException e) {

        String key = e.getEntityClass().getSimpleName();

        String value = "id " + e.getId() + " not found";

        Map<String, String> body = Collections.singletonMap(key, value);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(Exception e) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "An unexpected error occurred. Please contact support.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Object> handleIllegalStateException(IllegalStateException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
    }

}
