package org.kiru.alarm_service.amazonsqs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushMessage {
    private String title;
    private String body;
    private String deviceToken;
    private Map<String, String> data;
}
