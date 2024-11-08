package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class ConflictException extends ContactoException {
    public ConflictException() {
        super(FailureCode.CONFLICT);
    }

    public ConflictException(FailureCode failureCode) {
        super(failureCode);
    }
}
