package org.kiru.core.device.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class Device {
    @NotNull
    private String firebaseToken;

    @NotNull
    private Long userId;

    @NotNull
    private String deviceType;

    @NotNull
    private String deviceId;

    public static Device of(String firebaseToken, Long userId, String deviceType, String deviceId) {
        return Device.builder()
                .firebaseToken(firebaseToken)
                .userId(userId)
                .deviceType(deviceType)
                .deviceId(deviceId)
                .build();
    }
}
