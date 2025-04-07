package org.kiru.alarm.alarm.in.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class UpdateDeviceReq {
    @NotNull
    private String firebaseToken;

    @NotNull
    private String deviceType;

    @NotNull
    private String deviceId;

    public static UpdateDeviceReq of(String firebaseToken, String deviceType, String deviceId) {
        return UpdateDeviceReq.builder()
                .firebaseToken(firebaseToken)
                .deviceType(deviceType)
                .deviceId(deviceId)
                .build();
    }
}
