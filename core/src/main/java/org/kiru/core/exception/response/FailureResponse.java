package org.kiru.core.exception.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureResponse {

    private String message;
    private HttpStatus status;
    private List<FieldError> errors;
    private String code;

    public FailureResponse(final FailureCode code, final List<FieldError> errors) {
        this.message = code.getMessage();
        this.status = code.getHttpStatus();
        this.errors = errors;
        this.code = code.getCode();
    }

    public static FailureResponse of(final FailureCode code, final BindingResult bindingResult) {
        return FailureResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .status(code.getHttpStatus())
                .errors(FieldError.of(bindingResult))
                .build();
    }

    public static FailureResponse of(final FailureCode code) {
        return FailureResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .status(code.getHttpStatus())
                .errors(new ArrayList<>())
                .build();
    }

    public static FailureResponse of(final FailureCode code, final List<FieldError> errors) {
        return FailureResponse.builder()
                .code(code.getCode())
                .message(code.getMessage())
                .status(code.getHttpStatus())
                .errors(errors)
                .build();
    }

    public static FailureResponse of(MethodArgumentTypeMismatchException e) {
        final String value = e.getValue() == null ? "" : e.getValue().toString();
        final List<FieldError> errors = FieldError.of(e.getName(), value,
                e.getErrorCode());
        return FailureResponse.of(FailureCode.INVALID_TYPE_VALUE, errors);
    }

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @Builder
    public static class FieldError {
        private String field;
        private String value;
        private String reason;

        private FieldError(final String field, final String value, final String reason) {
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

        public static List<FieldError> of(final String field, final String value, final String reason) {
            List<FieldError> fieldErrors = new ArrayList<>();
            fieldErrors.add(new FieldError(field, value, reason));
            return fieldErrors;
        }

        public static List<FieldError> of(final BindingResult bindingResult) {
            final List<org.springframework.validation.FieldError> fieldErrors = bindingResult.getFieldErrors();
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .toList();
        }

        public static List<FieldError> of(List<org.springframework.validation.FieldError> fieldErrors) {
            return fieldErrors.stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getRejectedValue() == null ? "" : error.getRejectedValue().toString(),
                            error.getDefaultMessage()))
                    .toList();
        }

        public static List<FieldError> to(List<ParameterValidationResult> errors) {
            return errors.stream()
                    .map(error -> new FieldError(
                            error.getMethodParameter().getParameterName(),
                            error.getArgument() == null ? "" : error.getArgument().toString(),
                            error.getResolvableErrors().getFirst().getDefaultMessage()))
                    .toList();
        }
    }
}