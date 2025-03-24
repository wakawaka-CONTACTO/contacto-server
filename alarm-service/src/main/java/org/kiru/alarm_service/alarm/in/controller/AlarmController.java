package org.kiru.alarm_service.alarm.in.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.alarm_service.alarm.in.dto.request.CreatedDeviceTokenReq;
import org.kiru.alarm_service.alarm.in.dto.response.CreatedDeviceTokenRes;
import org.kiru.alarm_service.service.AlarmService;
import org.kiru.core.devicetoken.domain.DeviceToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("/devicetoken")
    public ResponseEntity<CreatedDeviceTokenRes> addDeviceToken(@RequestBody CreatedDeviceTokenReq req) {
        // 디바이스 토큰 추가
        Long deviceTokenId = alarmService.createDeviceToken(DeviceToken.of(req.getDeviceToken(), req.getUserId()));
        return ResponseEntity.ok(CreatedDeviceTokenRes.of(deviceTokenId));
    }
}
