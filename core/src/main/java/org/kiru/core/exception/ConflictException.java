package org.kiru.core.exception;


import org.kiru.core.exception.code.FailureCode;

public class ConflictException extends ContactoException {
    public ConflictException() {
        super(FailureCode.CONFLICT);
    }

    public ConflictException(FailureCode failureCode) {
        super(failureCode);
    }
}
