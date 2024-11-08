package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class BadRequestException extends ContactoException {
    public BadRequestException() {
        super(FailureCode.BAD_REQUEST);
    }

    public BadRequestException(FailureCode failureCode) {
        super(failureCode);
    }
}
