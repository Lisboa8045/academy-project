package com.academy.config;

public class TestTokenStorage {

    private String lastToken;

    public void storeToken(String token) {
        this.lastToken = token;
    }

    public String getLastToken() {
        return lastToken;
    }

    public void clear() {
        this.lastToken = null;
    }
}
