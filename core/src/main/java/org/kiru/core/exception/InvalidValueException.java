package org.kiru.core.exception;


import org.kiru.core.exception.code.FailureCode;

public class InvalidValueException extends ContactoException {
    public InvalidValueException() {
        super(FailureCode.BAD_REQUEST);
    }

    public InvalidValueException(FailureCode failureCode) {
        super(failureCode);
    }
}
