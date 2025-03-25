package org.kiru.alarm_service.alarm.in.dto.response;

import lombok.Builder;

@Builder
public record CreatedDeviceRes(
        Long deviceTokenId
){
    public static CreatedDeviceRes of(final Long deviceTokenId) {
        return CreatedDeviceRes.builder()
                .deviceTokenId(deviceTokenId)
                .build();
    }
}
