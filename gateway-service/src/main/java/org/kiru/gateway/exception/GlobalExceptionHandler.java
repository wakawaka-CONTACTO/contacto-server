package org.kiru.gateway.exception;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.BadRequestException;
import org.kiru.core.exception.UnauthorizedException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.exception.response.FailureResponse;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import reactor.core.publisher.Mono;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Mono<ResponseEntity<FailureResponse>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException", e);
        BindingResult bindingResult = e.getBindingResult();
        List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of(bindingResult);
        FailureResponse response = new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(BindException.class)
    public Mono<ResponseEntity<FailureResponse>> handleBindException(BindException e) {
        log.error("BindException", e);
        FailureResponse response = FailureResponse.of(FailureCode.INVALID_INPUT_VALUE, e.getBindingResult());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Mono<ResponseEntity<FailureResponse>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.error("MethodArgumentTypeMismatchException", e);
        String value = e.getValue() == null ? "" : e.getValue().toString();
        List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of(e.getName(), value, e.getErrorCode());
        FailureResponse response = new FailureResponse(FailureCode.INVALID_TYPE_VALUE, errors);
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    @ExceptionHandler(NotFoundException.class)
    public Mono<ResponseEntity<FailureResponse>> handleNotFoundException(NotFoundException e) {
        log.error("NotFoundException", e);
        String errorMessage = e.getMessage();
        List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of(e.getReason(), e.getBody().getDetail(), errorMessage);
        FailureResponse response = FailureResponse.of(FailureCode.SERVICE_UNAVAILABLE, errors);
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public Mono<ResponseEntity<FailureResponse>> handleGeneralException(UnauthorizedException e) {
        log.error("AuthorizationError", e);
        FailureResponse response = FailureResponse.of(e.getFailureCode());
        return Mono.just(ResponseEntity.status(e.getFailureCode().getHttpStatus()).body(response));
    }

    @ExceptionHandler(BadRequestException.class)
    public Mono<ResponseEntity<FailureResponse>> handleGeneralException(BadRequestException e) {
        log.error("BadRequestException", e);
        FailureResponse response = FailureResponse.of(e.getFailureCode());
        return Mono.just(ResponseEntity.status(e.getFailureCode().getHttpStatus()).body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<FailureResponse>> handleGeneralException(Exception e) {
        log.error("Exception", e);
        String errorMessage = e.getMessage() != null ? e.getMessage() : "Internal Server Error";
        List<FailureResponse.FieldError> errors = FailureResponse.FieldError.of("Exception", "", errorMessage);
        FailureResponse response = FailureResponse.of(FailureCode.INTERNAL_SERVER_ERROR, errors);
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}