package com.academy.exceptions;

import lombok.Getter;

public class MemberNotFoundByEmailException extends RuntimeException {
    @Getter
    private final String email;

    public MemberNotFoundByEmailException(String email) {
        super("Member with email " + email + " not found");
        this.email = email;
    }
}
