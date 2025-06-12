package com.academy.exceptions;

import lombok.Getter;

import java.util.Map;

@Getter
public class RegistrationConflictException extends RuntimeException {
    private final Map<String, String> fieldErrors;

    public RegistrationConflictException(Map<String, String> fieldErrors) {
        super("Registration conflict occurred.");
        this.fieldErrors = fieldErrors;
    }

}