package org.kiru.alarm.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
        log.info("📱 새로운 디바이스 등록 시도 - userId: {}, deviceId: {}, deviceType: {}, firebaseToken: {}", 
            device.getUserId(), device.getDeviceId(), device.getDeviceType(), device.getFirebaseToken());
        DeviceJpaEntity existingDevice = findDevice(device.getUserId(), device.getDeviceId());

        if(existingDevice == null) {
            log.info("✅ 새로운 디바이스 등록 완료 - userId: {}, deviceId: {}", device.getUserId(), device.getDeviceId());
            return deviceRepository.save(DeviceJpaEntity.of(device));
        }else if (isFirebaseTokenChanged(device, existingDevice)) {
            log.info("🔄 Firebase 토큰 업데이트 - userId: {}, deviceId: {}, oldToken: {}, newToken: {}", 
                device.getUserId(), device.getDeviceId(), existingDevice.getFirebaseToken(), device.getFirebaseToken());
            existingDevice.updateFirebaseToken(device.getFirebaseToken());
            return deviceRepository.save(existingDevice);
        }

        log.info("ℹ️ 디바이스 변경 없음 - userId: {}, deviceId: {}", device.getUserId(), device.getDeviceId());
        return null;
    }

    public void sendMessageAll(String title, String body, Map<String, String> content) {
        log.info("📢 전체 메시지 전송 시작 - title: {}, body: {}, content: {}", title, body, content);
        List<String> allFirebaseTokens = deviceRepository.findAllDistinctFirebaseTokens();
        log.info("📱 전체 메시지 전송 대상 디바이스 수: {}", allFirebaseTokens.size());
        sendMessagesToTokens(allFirebaseTokens, title, body, content, "all devices");
    }

    public void sendMessageToUser(Long userId, String title, String body, Map<String, String> content) {
        log.info("📢 사용자 메시지 전송 시작 - userId: {}, title: {}, body: {}, content: {}", userId, title, body, content);
        List<String> firebaseTokens = deviceRepository.findFirebaseTokensByUserId(userId);
        if (!firebaseTokens.isEmpty()) {
            log.info("📱 사용자 디바이스 수: {} - userId: {}", firebaseTokens.size(), userId);
            List<String> validTokens = firebaseTokens.stream()
                    .filter(token -> token != null)
                    .toList();
            log.info("✅ 유효한 토큰 수: {} - userId: {}", validTokens.size(), userId);
            sendMessagesToTokens(validTokens, title, body, content, "user " + userId);
        } else {
            log.warn("⚠️ 사용자 디바이스 없음 - userId: {}", userId);
        }
    }

    private void sendMessagesToTokens(List<String> firebaseTokens, String title, String body, Map<String, String> content, String target) {
        for (String firebaseToken : firebaseTokens) {
            log.info("📲 메시지 전송 중 - target: {}, token: {}, title: {}, body: {}", target, firebaseToken, title, body);
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
            log.info("🔥 FCM 전송 시도 - token: {}, title: {}, body: {}, content: {}", firebaseToken, title, body, content);
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

            FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("✅ FCM 전송 성공 - token: {}", firebaseToken);
        } catch (Exception e) {
            log.error("❌ FCM 전송 실패 - token: {}, error: {}", firebaseToken, e.getMessage(), e);
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
