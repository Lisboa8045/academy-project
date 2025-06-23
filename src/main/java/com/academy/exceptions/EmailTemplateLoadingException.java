package com.academy.exceptions;

public class EmailTemplateLoadingException extends RuntimeException {
    public EmailTemplateLoadingException(String message) {
        super(message);
    }
}
