package org.kiru.alarm.alarm.in.repository;

import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceJpaEntity,Long> {
    @Query("SELECT DISTINCT d.firebaseToken FROM DeviceJpaEntity d WHERE d.firebaseToken IS NOT NULL")
    List<String> findAllDistinctFirebaseTokens();
    @Query("SELECT d FROM DeviceJpaEntity d WHERE d.userId = :userId AND d.deviceId = :deviceId")
    DeviceJpaEntity findByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
}
