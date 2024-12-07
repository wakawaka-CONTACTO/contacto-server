package org.kiru.user.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.kiru.core.exception.response.FailureResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class FeignErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;

    public FeignErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatus status = HttpStatus.valueOf(response.status());
        FailureResponse failureResponse = null;
        try (InputStream bodyIs = response.body().asInputStream()) {
            failureResponse = objectMapper.readValue(bodyIs, FailureResponse.class);
        } catch (IOException e) {
            return new RuntimeException("REQUEST FAILED...");
        }
        return new FeignClientException(failureResponse);
    }
}