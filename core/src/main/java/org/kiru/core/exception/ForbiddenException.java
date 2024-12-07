package org.kiru.core.exception;


import org.kiru.core.exception.code.FailureCode;

public class ForbiddenException extends ContactoException {
    public ForbiddenException() {
        super(FailureCode.FORBIDDEN);
    }

    public ForbiddenException(FailureCode failureCode) {
        super(failureCode);
    }
}
