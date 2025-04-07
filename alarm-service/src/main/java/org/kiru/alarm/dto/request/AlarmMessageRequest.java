package org.kiru.alarm.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmMessageRequest {
    private String title;
    private String body;

    public static AlarmMessageRequest of(String title, String body) {
        return AlarmMessageRequest.builder()
                .title(title)
                .body(body)
                .build();
    }
} 