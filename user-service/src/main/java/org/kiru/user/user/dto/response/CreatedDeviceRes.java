package org.kiru.user.user.dto.response;

import lombok.Builder;

@Builder
public record CreatedDeviceRes(
        boolean madeDevice
){
    public static CreatedDeviceRes of(final boolean madeDevice) {
        return CreatedDeviceRes.builder()
                .madeDevice(madeDevice)
                .build();
    }
}

