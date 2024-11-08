package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class UnauthorizedException extends ContactoException {
    public UnauthorizedException() {
        super(FailureCode.UNAUTHORIZED);
    }

    public UnauthorizedException(FailureCode failureCode) {
        super(failureCode);
    }
}
