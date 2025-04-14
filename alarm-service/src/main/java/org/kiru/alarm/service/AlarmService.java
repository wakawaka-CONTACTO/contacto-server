package org.kiru.alarm.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.kiru.alarm.dto.request.UpdateDeviceReq;
import org.kiru.alarm.repository.DeviceRepository;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public void sendMessageAll(String title, String body) {
        List<String> allFirebaseTokens = deviceRepository.findAllDistinctFirebaseTokens();
        sendMessagesToTokens(allFirebaseTokens, title, body, "all devices");
    }

    public void sendMessageToUser(Long userId, String title, String body) {
        List<String> firebaseTokens = deviceRepository.findFirebaseTokensByUserId(userId);
        if (!firebaseTokens.isEmpty()) {
            log.info("📲Sending message to user: {} with {} devices", userId, firebaseTokens.size());
            List<String> validTokens = firebaseTokens.stream()
                    .filter(token -> token != null)
                    .toList();
            sendMessagesToTokens(validTokens, title, body, "user " + userId);
        } else {
            log.info("No devices found for user: {}", userId);
        }
    }

    private void sendMessagesToTokens(List<String> firebaseTokens, String title, String body, String target) {
        for (String firebaseToken : firebaseTokens) {
            log.info("📲Sending message to {}: {}", target, firebaseToken);
            sendFcm(firebaseToken, title, body);
        }
    }

    private List<DeviceJpaEntity> findByDeviceId(String deviceId) {
        log.info("Finding device with deviceId: {}", deviceId);
        List<DeviceJpaEntity> devices = deviceRepository.findByDeviceId(deviceId);
        if (devices == null || devices.isEmpty()) {
            log.info("No device found with deviceId: {}", deviceId);
        } else {
            log.info("Found devices: {}", devices);
        }
        return devices;
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

    private void sendFcm(String firebaseToken, String title, String body) {
        try {
            Message fcmMessage = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(firebaseToken)
                    .build();

            FirebaseMessaging.getInstance().send(fcmMessage);
        } catch (Exception e) {
            log.error("❌ FCM 전송 실패", e);
        }
    }
    @Transactional
    public void  updateDevice(UpdateDeviceReq req) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        List<DeviceJpaEntity> existingDevices = findByDeviceId(req.getDeviceId());
        for(DeviceJpaEntity deviceJpaEntity: existingDevices) {
            log.info("{} Updating device with userId: {} and deviceId: {}", now,deviceJpaEntity.getUserId(), req.getDeviceId());
           deviceJpaEntity.updateFirebaseToken(req.getFirebaseToken());
        }
        deviceRepository.saveAll(existingDevices);
    }

    @Transactional
    public void deleteDevice(Long userId, String deviceId) {
        DeviceJpaEntity device = findDevice(userId, deviceId);
        if (device != null) {
            deviceRepository.delete(device);
            log.info("Device deleted successfully - userId: {}, deviceId: {}", userId, deviceId);
        } else {
            log.info("Device not found - userId: {}, deviceId: {}", userId, deviceId);
        }
    }
}
