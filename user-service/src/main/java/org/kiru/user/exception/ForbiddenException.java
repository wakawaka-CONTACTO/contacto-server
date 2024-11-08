package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class ForbiddenException extends ContactoException {
    public ForbiddenException() {
        super(FailureCode.FORBIDDEN);
    }

    public ForbiddenException(FailureCode failureCode) {
        super(failureCode);
    }
}
