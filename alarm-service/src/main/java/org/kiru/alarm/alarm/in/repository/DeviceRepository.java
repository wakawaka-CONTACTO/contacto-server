package org.kiru.alarm.alarm.in.repository;

import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceJpaEntity,Long> {
    public DeviceJpaEntity findByUserId(Long userId);
    public DeviceJpaEntity findByUserIdAndDeviceId(Long userId, String deviceId);
}
