package org.kiru.user.common;

import lombok.Getter;
import org.kiru.core.exception.response.FailureResponse;

@Getter
public class FeignClientException extends RuntimeException {
    private final FailureResponse failureResponse;

    public FeignClientException(FailureResponse failureResponse) {
        super(failureResponse.getMessage());
        this.failureResponse = failureResponse;
    }
}