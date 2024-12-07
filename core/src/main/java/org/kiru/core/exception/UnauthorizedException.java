package org.kiru.core.exception;


import org.kiru.core.exception.code.FailureCode;

public class UnauthorizedException extends ContactoException {
    public UnauthorizedException() {
        super(FailureCode.UNAUTHORIZED);
    }

    public UnauthorizedException(FailureCode failureCode) {
        super(failureCode);
    }
}
