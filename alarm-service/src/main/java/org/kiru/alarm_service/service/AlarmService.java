package org.kiru.alarm_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.alarm_service.alarm.in.repository.AlarmRepository;
import org.kiru.alarm_service.amazonsqs.AwsSqsNotificationSender;
import org.kiru.alarm_service.amazonsqs.dto.PushMessage;
import org.kiru.core.devicetoken.domain.DeviceToken;
import org.kiru.core.devicetoken.entity.DeviceTokenJpaEntity;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AwsSqsNotificationSender awsSqsNotificationSender;

    public Long createDeviceToken(DeviceToken deviceToken) {
        // userId와 deviceToken을 저장
        log.info("deviceToken : {}", deviceToken);
        return alarmRepository.save(DeviceTokenJpaEntity.of(deviceToken)).getId();
    }

    private String getDeviceToken(Long recieverId) {
        // recieverId에 해당하는 DeviceToken을 조회
        return alarmRepository.findByUserId(recieverId).getDeviceToken();
    }


}
