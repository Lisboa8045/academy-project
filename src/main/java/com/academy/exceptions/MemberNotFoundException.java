package com.academy.exceptions;


import lombok.Getter;

public class MemberNotFoundException extends RuntimeException {
    @Getter
    private final String username;

    public MemberNotFoundException(String username) {
        super("Member with username " + username + " not found");
        this.username = username;
    }
}
