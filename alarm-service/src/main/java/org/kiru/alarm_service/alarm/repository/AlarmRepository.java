package org.kiru.alarm_service.alarm.repository;

import org.kiru.core.devicetoken.entity.DeviceTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<DeviceTokenJpaEntity,Long> {
    public DeviceTokenJpaEntity findByUserId(Long userId);
}
