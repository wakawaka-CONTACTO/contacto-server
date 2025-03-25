package org.kiru.alarm_service.alarm.in.repository;

import org.kiru.core.devicetoken.entity.DeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmRepository extends JpaRepository<DeviceJpaEntity,Long> {
    public DeviceJpaEntity findByUserId(Long userId);
}
