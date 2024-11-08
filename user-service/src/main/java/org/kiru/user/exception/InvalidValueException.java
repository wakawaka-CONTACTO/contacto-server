package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class InvalidValueException extends ContactoException {
    public InvalidValueException() {
        super(FailureCode.BAD_REQUEST);
    }

    public InvalidValueException(FailureCode failureCode) {
        super(failureCode);
    }
}
