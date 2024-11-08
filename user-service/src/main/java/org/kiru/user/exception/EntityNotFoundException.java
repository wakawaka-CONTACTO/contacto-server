package org.kiru.user.exception;


import org.kiru.user.exception.code.FailureCode;

public class EntityNotFoundException extends ContactoException {
    public EntityNotFoundException() {
        super(FailureCode.ENTITY_NOT_FOUND);
    }

    public EntityNotFoundException(FailureCode failureCode) {
        super(failureCode);
    }
}
