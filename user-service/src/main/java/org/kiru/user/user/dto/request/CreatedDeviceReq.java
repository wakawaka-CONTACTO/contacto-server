package org.kiru.user.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class CreatedDeviceReq {
        @NotNull
        private Long userId;

        @NotNull
        private String deviceToken;

        @NotNull
        private String deviceType;

        @NotNull
        private String deviceId;

        public static CreatedDeviceReq of(Long userId, String deviceToken, String deviceType, String deviceId) {
            return CreatedDeviceReq.builder()
                    .userId(userId)
                    .deviceToken(deviceToken)
                    .deviceType(deviceType)
                    .deviceId(deviceId)
                    .build();
        }
}
