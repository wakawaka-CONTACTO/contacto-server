package org.kiru.alarm_service.amazonsqs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushMessage {
    @NotNull
    private String title;
    @NotNull
    private String body;
    @NotNull
    private String deviceToken;

    private Map<String, String> data;
}
