package org.kiru.user.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CreatedDeviceReq {
    Long userId;
    String deviceToken;
    String deviceType;
    String deviceId;

    public static CreatedDeviceReq of(Long userId, String deviceToken, String deviceType, String deviceId) {
        return CreatedDeviceReq.builder()
                .userId(userId)
                .deviceToken(deviceToken)
                .deviceType(deviceType)
                .deviceId(deviceId)
                .build();
    }
}
