package org.kiru.core.exception;

import lombok.Getter;
import org.kiru.core.exception.code.FailureCode;

@Getter
public class ContactoException extends RuntimeException {
    private final FailureCode failureCode;

    public ContactoException(FailureCode failureCode) {
        super(failureCode.getMessage());
        this.failureCode = failureCode;
    }
}
