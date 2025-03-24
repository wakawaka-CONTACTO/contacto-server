package org.kiru.alarm_service.alarm.in.controller;

import lombok.RequiredArgsConstructor;
import org.kiru.alarm_service.alarm.in.dto.req.CreatedDeviceTokenReq;
import org.kiru.alarm_service.service.AlarmService;
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

    @PostMapping("/devicetoken")
    public void addDeviceToken(@RequestBody CreatedDeviceTokenReq req) {
        // 디바이스 토큰 추가
        alarmService.createDeviceToken(DeviceToken.of(req.getDeviceToken(), req.getUserId()));
    }
}
