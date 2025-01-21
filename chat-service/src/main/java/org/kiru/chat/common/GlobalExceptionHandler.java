package org.kiru.chat.common;

import io.micrometer.tracing.SpanName;
import io.micrometer.tracing.annotation.ContinueSpan;
import io.opentelemetry.api.trace.Span;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.exception.response.FailureResponse;
import org.kiru.core.exception.response.FailureResponse.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@ControllerAdvice
@SpanName("Exception")
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ContinueSpan(log = "Error")
    public ResponseEntity<FailureResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        final BindingResult bindingResult = e.getBindingResult();
        log.error(">>> handle: MethodArgumentNotValidException ", e);
        final List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of(bindingResult);
        FailureResponse response = new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(response);
    }

    /**
     * Handles Spring's BindException for input validation errors.
     *
     * This method is triggered when data binding fails during request processing,
     * typically due to type conversion or validation constraint violations.
     *
     * @param e The BindException containing details about the binding errors
     * @return A ResponseEntity with a BAD_REQUEST status, including a FailureResponse
     *         with specific validation error details and trace/span identifiers
     *
     * @see BindException
     * @see FailureResponse
     */
    @ContinueSpan(log = "Error")
    @ExceptionHandler(BindException.class)
    protected ResponseEntity<FailureResponse> handleBindException(final BindException e) {
        log.error(">>> handle: BindException ", e);
        final FailureResponse response = FailureResponse.of(FailureCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(response);
    }

    /**
     * Handles Feign client exceptions by creating an appropriate HTTP response.
     *
     * This method processes {@link FeignClientException} instances, logging the error
     * and constructing a response with the original failure response's status code.
     *
     * @param e The Feign client exception to be handled
     * @return A {@link ResponseEntity} containing the failure response with trace and span headers
     */
    @ContinueSpan(log="Error")
    @ExceptionHandler(FeignClientException.class)
    public ResponseEntity<FailureResponse> handleFeignClientException(FeignClientException e) {
        log.error(">>> handle: FeignClientException ", e);
        return ResponseEntity
                .status(HttpStatus.valueOf(e.getFailureResponse().getStatus().value()))
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(e.getFailureResponse());
    }

    /**
     * Handles type mismatch exceptions during method argument binding.
     *
     * This method is triggered when a method argument cannot be converted to the expected type.
     * It logs the error, creates a detailed failure response with field-level error information,
     * and returns a BAD REQUEST (400) HTTP response.
     *
     * @param e The {@link MethodArgumentTypeMismatchException} representing the type conversion error
     * @return A {@link ResponseEntity} containing a {@link FailureResponse} with error details
     *         and HTTP status BAD REQUEST
     */
    @ContinueSpan(log = "Error")
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailureResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        final List<FieldError> errors = FailureResponse.FieldError.of(e.getName(), value, e.getErrorCode());
        log.error(">>> handle: MethodArgumentTypeMismatchException ", e);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors));
    }

    @ContinueSpan(log = "Error")
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<FailureResponse> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException e) {
        log.error(">>> handle: HttpRequestMethodNotSupportedException ", e);
        final FailureResponse response = FailureResponse.of(FailureCode.METHOD_NOT_ALLOWED);
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(response);
    }

    @ContinueSpan(log = "Error")
    @ExceptionHandler(ContactoException.class)
    public ResponseEntity<FailureResponse> handleContactoException(final ContactoException e) {
        log.error(">>> handle: ContactoException ", e);
        final FailureCode errorCode = e.getFailureCode();
        final FailureResponse response = FailureResponse.of(errorCode);
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(response);
    }

    @ContinueSpan(log = "Error")
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<FailureResponse> handleException(final Exception e) {
        log.error(">>> handle: Exception ", e);
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
        List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of("Exception", "", errorMessage);
        final FailureResponse response = FailureResponse.of(FailureCode.INTERNAL_SERVER_ERROR, errors);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("X-Trace-Id", getCurrentTraceId())
                .header("X-Span-Id", getCurrentSpanId(e))
                .body(response);
    }

    private String getCurrentTraceId() {
        return Span.current().getSpanContext().getTraceId();
    }

    private String getCurrentSpanId(Throwable e) {
        Span currentSpan = Span.current();
        currentSpan.setAttribute("error", e.getMessage());
        currentSpan.recordException(e);
        return currentSpan.getSpanContext().getSpanId();
    }
}