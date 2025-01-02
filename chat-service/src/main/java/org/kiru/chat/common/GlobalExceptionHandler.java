package org.kiru.chat.common;

import java.util.List;
import java.util.NoSuchElementException;
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
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<FailureResponse> handleValidationExceptions(MethodArgumentNotValidException e) {
        final BindingResult bindingResult = e.getBindingResult();
        final List<FieldError> errors = FieldError.of(bindingResult);
        FailureResponse response = new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BindException.class)
    protected ResponseEntity<FailureResponse> handleBindException(final BindException e) {
        log.error(">>> handle: BindException ", e);
        final FailureResponse response = FailureResponse.of(FailureCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<FailureResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        final List<FieldError> errors = FieldError.of(e.getName(), value, e.getErrorCode());
        return new ResponseEntity<>(new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors),
                HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<FailureResponse> handleHttpRequestMethodNotSupportedException(
            final HttpRequestMethodNotSupportedException e) {
        log.error(">>> handle: HttpRequestMethodNotSupportedException ", e);
        final FailureResponse response = FailureResponse.of(FailureCode.METHOD_NOT_ALLOWED);
        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(ContactoException.class)
    public ResponseEntity<FailureResponse> handleContactoException(final ContactoException e) {
        log.error(">>> handle: ContactoException ", e);
        final FailureCode errorCode = e.getFailureCode();
        final FailureResponse response = FailureResponse.of(errorCode);
        return new ResponseEntity<>(response, errorCode.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);
        FailureResponse failureResponse = FailureResponse.builder()
                .message(e.getMessage())
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .code(FailureCode.INTERNAL_SERVER_ERROR.getCode())
                .errors(
                        FieldError.of(
                                e.getClass().getName(),
                                e.getLocalizedMessage(),
                                e.getClass().getName()
                        )
                ).build();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failureResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<FailureResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Invalid argument error", e);
        FailureResponse failureResponse = FailureResponse.builder()
                .message(e.getMessage())
                .status(HttpStatus.BAD_REQUEST)
                .code(FailureCode.INVALID_INPUT_VALUE.getCode())
                .errors(
                        FieldError.of(
                                e.getClass().getName(),
                                e.getLocalizedMessage(),
                                e.getClass().getName()
                        )
                ).build();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(failureResponse);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<FailureResponse> handleNoSuchElementException(NoSuchElementException e) {
        StackTraceElement errorLocation = e.getStackTrace()[0];
        String errorSource = String.format("%s.%s(%s:%d)",
                errorLocation.getClassName(),
                errorLocation.getMethodName(),
                errorLocation.getFileName(),
                errorLocation.getLineNumber()
        );
        log.error("No such element error in {}", errorSource, e);
        FailureResponse failureResponse = FailureResponse.builder()
                .message("요청한 리소스를 찾을 수 없습니다.")
                .status(HttpStatus.NOT_FOUND)
                .code(FailureCode.RESOURCE_NOT_FOUND.getCode())
                .errors(
                        FieldError.of(
                                e.getClass().getName(),
                                e.getMessage(),
                                errorSource
                        )
                ).build();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(failureResponse);
    }
}