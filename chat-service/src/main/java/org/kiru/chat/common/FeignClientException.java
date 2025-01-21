package org.kiru.chat.common;


import lombok.Getter;
import org.kiru.core.exception.response.FailureResponse;

@Getter
public class FeignClientException extends RuntimeException {
    private final FailureResponse failureResponse;

    /**
     * Constructs a new FeignClientException with the specified FailureResponse.
     *
     * @param failureResponse the FailureResponse containing error details and message
     * @throws IllegalArgumentException if the provided failureResponse is null
     */
    public FeignClientException(FailureResponse failureResponse) {
        super(failureResponse.getMessage());
        this.failureResponse = failureResponse;
    }
}