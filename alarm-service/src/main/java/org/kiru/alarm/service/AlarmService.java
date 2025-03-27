package org.kiru.alarm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.alarm.alarm.in.repository.DeviceRepository;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final DeviceRepository deviceRepository;

    public DeviceJpaEntity createDevice(Device device) {
        DeviceJpaEntity existingDevice = findDevice(device.getUserId(), device.getDeviceId());

        if(existingDevice == null) {
            return deviceRepository.save(DeviceJpaEntity.of(device));
        }else if (validFirebaseToken(device, existingDevice)) {
           existingDevice.updateFirebaseToken(device.getFirebaseToken());
            return deviceRepository.save(existingDevice);
        }

        return null;
    }

    public void sendMessageAll(String message) {

        // 단일 디바이스 FCM 토큰
        List<DeviceJpaEntity> allDeviceList = deviceRepository.findAll();

        for (DeviceJpaEntity device : allDeviceList) {
            sendFcm(device.getFirebaseToken(), message);
        }

    }

    private DeviceJpaEntity findDevice(Long userId, String deviceId) {
        return deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
    }

    private boolean validFirebaseToken(Device newDevice, DeviceJpaEntity existingDevice){
        return !newDevice.getFirebaseToken().equals(existingDevice.getFirebaseToken());
    }
    private void sendFcm(String firebaseToken, String message) {
        try {
            Message fcmMessage = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle("알람")
                            .setBody(message)
                            .build())
                    .setToken(firebaseToken)
                    .build();

            FirebaseMessaging.getInstance().send(fcmMessage);
        } catch (Exception e) {
            log.error("❌ FCM 전송 실패", e);
        }
    }
}
