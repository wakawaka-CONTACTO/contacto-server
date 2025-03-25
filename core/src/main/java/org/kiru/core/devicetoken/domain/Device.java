package org.kiru.core.devicetoken.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Device {
    @NotNull
    private String deviceToken;

    @NotNull
    private Long userId;

    @NotNull
    private String deviceType;

    @NotNull
    private String deviceId;

    public static Device of(String deviceToken, Long userId, String deviceType, String deviceId) {
        return Device.builder()
                .deviceToken(deviceToken)
                .userId(userId)
                .deviceType(deviceType)
                .deviceId(deviceId)
                .build();
    }
}
