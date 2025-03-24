package org.kiru.alarm_service.alarm.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CreatedDeviceTokenReq {
    Long userId;
    String deviceToken;

    public static CreatedDeviceTokenReq of(Long userId, String deviceToken) {
        return CreatedDeviceTokenReq.builder()
                .userId(userId)
                .deviceToken(deviceToken)
                .build();
    }
}

