package org.kiru.alarm.alarm.in.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.alarm.alarm.in.dto.request.CreatedDeviceReq;
import org.kiru.alarm.alarm.in.dto.response.CreatedDeviceRes;
import org.kiru.alarm.service.AlarmService;
import org.kiru.core.device.domain.Device;
import org.kiru.core.device.entity.DeviceJpaEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
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
    public ResponseEntity<String> sendMessageAll(@RequestBody String message) {
        alarmService.sendMessageAll(message);
        return ResponseEntity.ok("Success");
    }
}
