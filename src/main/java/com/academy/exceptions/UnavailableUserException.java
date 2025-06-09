package com.academy.exceptions;

import com.academy.models.member.MemberStatusEnum;
import lombok.Getter;

public class UnavailableUserException extends RuntimeException {
    @Getter
    private MemberStatusEnum memberStatus;

    public UnavailableUserException(MemberStatusEnum memberStatus) {
        super("Member is Inactive with status " + memberStatus);
        this.memberStatus = memberStatus;
    }
}
