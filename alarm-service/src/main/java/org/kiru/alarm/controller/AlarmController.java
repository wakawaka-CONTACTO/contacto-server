package org.kiru.alarm.controller;

import org.kiru.alarm.dto.request.AlarmMessageRequest;
import org.kiru.alarm.dto.request.CreatedDeviceReq;
import org.kiru.alarm.dto.request.UpdateDeviceReq;
import org.kiru.alarm.dto.response.CreatedDeviceRes;
import org.kiru.alarm.service.AlarmService;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {

    private final AlarmService alarmService;
    
    @PostMapping("/device")
    public ResponseEntity<CreatedDeviceRes> createDevice(@RequestBody CreatedDeviceReq req) {
        DeviceJpaEntity firebaseToken = alarmService.createDevice(
                Device.of(req.getFirebaseToken(), req.getUserId(), req.getDeviceType(), req.getDeviceId()));
        if (firebaseToken == null) {
            return ResponseEntity.ok(new CreatedDeviceRes(false));
        }
        return ResponseEntity.ok(new CreatedDeviceRes(true));
    }
    
    @PostMapping("/send/message/all")
    public ResponseEntity<String> sendMessageAll(@RequestBody AlarmMessageRequest message) {
        alarmService.sendMessageAll(message.getTitle(), message.getBody(), message.getContent());
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/device/update")
    public ResponseEntity<CreatedDeviceRes> updateDevice(@RequestBody UpdateDeviceReq req) {
        alarmService.updateDevice(req);
        return ResponseEntity.ok(new CreatedDeviceRes(true));
    }

    @DeleteMapping("/device/delete")
    public ResponseEntity<CreatedDeviceRes> deleteDevice(
            @RequestParam("userId") Long userId,
            @RequestParam("deviceId") String deviceId) {
        alarmService.deleteDevice(userId, deviceId);
        log.info("Device deleted - userId: {}, deviceId: {}", userId, deviceId);
        return ResponseEntity.ok(new CreatedDeviceRes(true));
    }

    @PostMapping("/send/message/user")
    public ResponseEntity<String> sendMessageToUser(
            @RequestParam("userId") Long userId,
            @RequestBody AlarmMessageRequest message) {
        alarmService.sendMessageToUser(userId, message.getTitle(), message.getBody(), message.getContent());
        return ResponseEntity.ok("Success");
    }
}
