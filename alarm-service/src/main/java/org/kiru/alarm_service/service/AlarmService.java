package org.kiru.alarm_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.alarm_service.alarm.in.repository.DeviceRepository;
import org.kiru.alarm_service.amazonsqs.AwsSqsNotificationSender;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final DeviceRepository deviceRepository;
    private final AwsSqsNotificationSender awsSqsNotificationSender;

    public Long createDevice(Device device) {
        DeviceJpaEntity existingDevice = findDevice(device.getUserId(), device.getDeviceId());

        if(existingDevice == null) {
            return deviceRepository.save(DeviceJpaEntity.of(device)).getId();
        }else if (validDeviceToken(device, existingDevice)) {
           existingDevice.updateDeviceToken(device.getDeviceToken());
            return deviceRepository.save(existingDevice).getId();
        }

        log.info("deviceToken : {}", device);

        return -1L;
    }

    private String getDeviceToken(Long recieverId) {
        return deviceRepository.findByUserId(recieverId).getDeviceToken();
    }

    private DeviceJpaEntity findDevice(Long userId, String deviceId) {
        return deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    private boolean validDeviceToken(Device newDevice, DeviceJpaEntity existingDevice){
        return !newDevice.getDeviceToken().equals(existingDevice.getDeviceToken());
    }
}
