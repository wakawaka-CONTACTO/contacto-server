package org.kiru.core.devicetoken.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.devicetoken.domain.Device;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Table(name = "device")
public class DeviceJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_token")
    private String deviceToken;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "device_id")
    private String deviceId;

    public static DeviceJpaEntity of(Device device) {
        return DeviceJpaEntity.builder()
                .deviceToken(device.getDeviceToken())
                .userId(device.getUserId())
                .deviceType(device.getDeviceType())
                .deviceId(device.getDeviceId())
                .build();
    }

    public static Device toModel(DeviceJpaEntity deviceJpaEntity) {
        return Device.builder()
                .deviceToken(deviceJpaEntity.getDeviceToken())
                .userId(deviceJpaEntity.getUserId())
                .deviceType(deviceJpaEntity.getDeviceType())
                .deviceId(deviceJpaEntity.getDeviceId())
                .build();
    }
}
