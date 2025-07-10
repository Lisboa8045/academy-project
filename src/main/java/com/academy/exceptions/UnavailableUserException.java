package com.academy.exceptions;

import com.academy.models.member.MemberStatusEnum;
import lombok.Getter;

public class UnavailableUserException extends RuntimeException {
    @Getter
    private MemberStatusEnum memberStatus;

    public UnavailableUserException(MemberStatusEnum memberStatus, String email) {
        super("Member is Inactive with status " + memberStatus + ":" + email);
        this.memberStatus = memberStatus;
    }
}
