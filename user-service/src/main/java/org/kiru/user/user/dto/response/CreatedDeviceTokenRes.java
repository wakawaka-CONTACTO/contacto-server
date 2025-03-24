package org.kiru.user.user.dto.response;

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

