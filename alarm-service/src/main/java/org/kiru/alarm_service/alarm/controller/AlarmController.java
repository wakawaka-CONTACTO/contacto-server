package org.kiru.alarm_service.alarm.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.alarm_service.alarm.dto.req.CreatedDeviceTokenReq;
import org.kiru.alarm_service.alarm.service.AlarmService;
import org.kiru.core.devicetoken.domain.DeviceToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @PostMapping("/match/{recieverId}")
    public void sendAlarm(@PathVariable("recieverId") Long recieverId) {
        // 매칭 알람 전송
        alarmService.sendMatchingAlarm(recieverId);
    }

    @PostMapping("/device")
    public void addDeviceToken(@RequestBody CreatedDeviceTokenReq req) {
        // 디바이스 토큰 추가
        alarmService.createDeviceToken(DeviceToken.of(req.getDeviceToken(), req.getUserId()));
    }
}
