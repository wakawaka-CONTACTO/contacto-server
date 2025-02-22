package org.kiru.core.exception.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode {

    OK(HttpStatus.OK, "s2000", "요청이 성공했습니다."),
    CREATED(HttpStatus.CREATED, "s2010", "요청이 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}