package org.kiru.alarm_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.alarm_service.alarm.in.repository.AlarmRepository;
import org.kiru.alarm_service.amazonsqs.AwsSqsNotificationSender;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AwsSqsNotificationSender awsSqsNotificationSender;

    public Long createDevice(Device device) {
        DeviceJpaEntity existingDevice = findDevice(device.getUserId(), device.getDeviceId());
        // 디바이스가 없을 경우 저장
        if(existingDevice == null) {
            return alarmRepository.save(DeviceJpaEntity.of(device)).getId();
        }else if (validDeviceToken(device, existingDevice)) {
            // 디바이스가 있을 경우 DeviceToken이 다를 경우 업데이트
           existingDevice.updateDeviceToken(device.getDeviceToken());
            return alarmRepository.save(existingDevice).getId();
        }

        // userId와 deviceToken을 저장
        log.info("deviceToken : {}", device);

        return -1L;
    }

    private String getDeviceToken(Long recieverId) {
        // recieverId에 해당하는 DeviceToken을 조회
        return alarmRepository.findByUserId(recieverId).getDeviceToken();
    }
    // 이미 있는 Device 인지 확인
    private DeviceJpaEntity findDevice(Long userId, String deviceId) {
        return alarmRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    private boolean validDeviceToken(Device newDevice, DeviceJpaEntity existingDevice){
        // DeviceToken이 유효한지 확인
        return !newDevice.getDeviceToken().equals(existingDevice.getDeviceToken());
    }
}
