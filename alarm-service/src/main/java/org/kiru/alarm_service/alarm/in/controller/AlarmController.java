package org.kiru.alarm_service.alarm.in.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.alarm_service.alarm.in.dto.request.CreatedDeviceReq;
import org.kiru.alarm_service.alarm.in.dto.response.CreatedDeviceRes;
import org.kiru.alarm_service.service.AlarmService;
import org.kiru.core.device.domain.Device;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("/device")
    public ResponseEntity<CreatedDeviceRes> createDevice(@RequestBody CreatedDeviceReq req) {
        Long deviceTokenId = alarmService.createDevice(
                Device.of(req.getDeviceToken(), req.getUserId(), req.getDeviceType(), req.getDeviceId()));
        return ResponseEntity.ok(CreatedDeviceRes.of(deviceTokenId));
    }
}
