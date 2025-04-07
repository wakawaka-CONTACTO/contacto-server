package org.kiru.alarm.repository;

import java.util.List;

import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<DeviceJpaEntity,Long> {
    @Query("SELECT DISTINCT d.firebaseToken FROM DeviceJpaEntity d WHERE d.firebaseToken IS NOT NULL")
    List<String> findAllDistinctFirebaseTokens();
    @Query("SELECT d FROM DeviceJpaEntity d WHERE d.userId = :userId AND d.deviceId = :deviceId")
    DeviceJpaEntity findByUserIdAndDeviceId(@Param("userId") Long userId, @Param("deviceId") String deviceId);
    @Query("SELECT d FROM DeviceJpaEntity d WHERE d.userId = :userId")
    List<DeviceJpaEntity> findByUserId(@Param("userId") Long userId);
    @Query("SELECT d.firebaseToken FROM DeviceJpaEntity d WHERE d.userId = :userId")
    List<String> findFirebaseTokensByUserId(@Param("userId") Long userId);
}
