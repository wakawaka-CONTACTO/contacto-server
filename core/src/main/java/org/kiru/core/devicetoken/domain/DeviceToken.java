package org.kiru.core.devicetoken.domain;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.devicetoken.entity.DeviceTokenJpaEntity;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class DeviceToken {
    @NotNull
    private String deviceToken;

    @NotNull
    private Long userId;

    public static DeviceToken of(String deviceToken, Long userId) {
        return DeviceToken.builder()
                .deviceToken(deviceToken)
                .userId(userId)
                .build();
    }
}
