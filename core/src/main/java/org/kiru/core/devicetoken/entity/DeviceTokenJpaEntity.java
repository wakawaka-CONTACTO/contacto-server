package org.kiru.core.devicetoken.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.devicetoken.domain.DeviceToken;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "device_tokens")
public class DeviceTokenJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "user_id")
    private Long userId;

    public static DeviceTokenJpaEntity of(DeviceToken deviceToken) {
        return DeviceTokenJpaEntity.builder()
                .id(deviceToken.getId())
                .deviceToken(deviceToken.getDeviceToken())
                .userId(deviceToken.getUserId())
                .build();
    }

    public static DeviceToken toModel(DeviceTokenJpaEntity deviceTokenJpaEntity) {
        return DeviceToken.builder()
                .id(deviceTokenJpaEntity.getId())
                .deviceToken(deviceTokenJpaEntity.getDeviceToken())
                .userId(deviceTokenJpaEntity.getUserId())
                .build();
    }
}
