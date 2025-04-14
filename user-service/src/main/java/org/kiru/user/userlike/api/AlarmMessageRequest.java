package org.kiru.user.userlike.api;

import java.util.HashMap;
import java.util.Map;

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
    @Builder.Default
    private Map<String, String> content = new HashMap<>();

    public static AlarmMessageRequest of(String title, String body) {
        return AlarmMessageRequest.builder()
                .title(title)
                .body(body)
                .build();
    }

    public static AlarmMessageRequest of(String title, String body, Map<String, String> content) {
        return AlarmMessageRequest.builder()
                .title(title)
                .body(body)
                .content(content)
                .build();
    }
} 