package org.kiru.user.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import io.micrometer.core.instrument.util.IOUtils;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.exception.response.FailureResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // 에러 응답 본문 파싱
            if (response.body() == null) {
                return new FeignClientException(
                        FailureResponse.builder()
                                .status(HttpStatus.valueOf(response.status()))
                                .message("Empty response body")
                                .code("FEIGN_EMPTY_BODY")
                                .build()
                );
            }
            String errorBody = IOUtils.toString(response.body().asInputStream(), StandardCharsets.UTF_8);
            FailureResponse failureResponse = parseErrorResponse(errorBody);
            return new FeignClientException(failureResponse);
        } catch (Exception e) {
            log.error("Feign error decoding failed", e);
            return new FeignClientException(
                    FailureResponse.builder()
                            .status(HttpStatus.valueOf(response.status()))
                            .message(e.getMessage()).code("FEIGN_COMMUNICATION_ERROR").build());
        }
    }

    private FailureResponse parseErrorResponse(String errorBody) {
        try {
            return objectMapper.readValue(errorBody, FailureResponse.class);
        } catch (Exception e) {
            return FailureResponse.builder()
                    .message(e.getCause().toString())
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .code(e.getMessage())
                    .build();
        }
    }
}