package com.academy.exceptions;

public class TagNotFoundException extends RuntimeException {
    public TagNotFoundException(Long id) {
        super("Tag with ID " + id + " not found.");
    }
}