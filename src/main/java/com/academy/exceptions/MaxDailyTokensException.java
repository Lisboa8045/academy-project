package com.academy.exceptions;

public class MaxDailyTokensException extends RuntimeException {
    public MaxDailyTokensException(String message) {
        super(message);
    }
}
