package org.kiru.core.exception;


import org.kiru.core.exception.code.FailureCode;

public class BadRequestException extends ContactoException {
    public BadRequestException() {
        super(FailureCode.BAD_REQUEST);
    }

    public BadRequestException(FailureCode failureCode) {
        super(failureCode);
    }
}