package org.kiru.alarm.service;

import java.util.List;
import java.util.Map;

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

    public void sendMessageAll(String title, String body, Map<String, String> content) {
        List<String> allFirebaseTokens = deviceRepository.findAllDistinctFirebaseTokens();
        sendMessagesToTokens(allFirebaseTokens, title, body, content, "all devices");
    }

    public void sendMessageToUser(Long userId, String title, String body, Map<String, String> content) {
        List<String> firebaseTokens = deviceRepository.findFirebaseTokensByUserId(userId);
        if (!firebaseTokens.isEmpty()) {
            log.info("📲Sending message to user: {} with {} devices", userId, firebaseTokens.size());
            List<String> validTokens = firebaseTokens.stream()
                    .filter(token -> token != null)
                    .toList();
            sendMessagesToTokens(validTokens, title, body, content, "user " + userId);
        } else {
            log.info("No devices found for user: {}", userId);
        }
    }

    private void sendMessagesToTokens(List<String> firebaseTokens, String title, String body, Map<String, String> content, String target) {
        for (String firebaseToken : firebaseTokens) {
            log.info("📲Sending message to {}: {}", target, firebaseToken);
            sendFcm(firebaseToken, title, body, content);
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

    private void sendFcm(String firebaseToken, String title, String body, Map<String, String> content) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setNotification(notification)
                    .setToken(firebaseToken);

            if (content != null && !content.isEmpty()) {
                messageBuilder.putAllData(content);
            }

            if (content != null && !content.isEmpty()) {
                messageBuilder.putAllData(content);
            }

            FirebaseMessaging.getInstance().send(messageBuilder.build());
        } catch (Exception e) {
            log.error("❌ FCM 전송 실패", e);
        }
    }
    @Transactional
    public DeviceJpaEntity updateDevice(UpdateDeviceReq req) {
        List<DeviceJpaEntity> existingDevices = findByDeviceId(req.getDeviceId());
        for(DeviceJpaEntity deviceJpaEntity: existingDevices) {
            deviceJpaEntity.updateFirebaseToken(req.getFirebaseToken());
        }
        deviceRepository.saveAll(existingDevices);
        return null;
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
