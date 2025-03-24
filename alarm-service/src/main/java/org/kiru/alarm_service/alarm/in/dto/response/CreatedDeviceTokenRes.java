package org.kiru.alarm_service.alarm.in.dto.response;

import lombok.Builder;

@Builder
public record CreatedDeviceTokenRes (
        Long deviceTokenId
){
    public static CreatedDeviceTokenRes of(final Long deviceTokenId) {
        return CreatedDeviceTokenRes.builder()
                .deviceTokenId(deviceTokenId)
                .build();
    }
}
