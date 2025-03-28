package org.kiru.alarm.service;

import java.util.List;

import org.kiru.alarm.alarm.in.repository.DeviceRepository;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final DeviceRepository deviceRepository;

    public DeviceJpaEntity createDevice(Device device) {
        DeviceJpaEntity existingDevice = findDevice(device.getUserId(), device.getDeviceId());

        if(existingDevice == null) {
            return deviceRepository.save(DeviceJpaEntity.of(device));
        }else if (isFirebaseTokenChanged(device, existingDevice)) {
           existingDevice.updateFirebaseToken(device.getFirebaseToken());
            return deviceRepository.save(existingDevice);
        }

        return null;
    }

    public void sendMessageAll(String message) {

        // 단일 디바이스 FCM 토큰
        List<String> allFirebaseTokens = deviceRepository.findAllDistinctFirebaseTokens();

        for (String firebaseToken : allFirebaseTokens) {
            log.info("📲Sending message to device: {}", firebaseToken);
            sendFcm(firebaseToken, message);
        }

    }

    private DeviceJpaEntity findDevice(Long userId, String deviceId) {
        log.info("Finding device with userId: {} and deviceId: {}", userId, deviceId);
        DeviceJpaEntity device = deviceRepository.findByUserIdAndDeviceId(userId, deviceId);
        if (device == null) {
            log.info("No device found with userId: {} and deviceId: {}", userId, deviceId);
        } else {
            log.info("Found device: {}", device);
        }
        return device;
    }

    private boolean isFirebaseTokenChanged(Device newDevice, DeviceJpaEntity existingDevice){
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
