package org.kiru.alarm_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.alarm_service.alarm.in.repository.AlarmRepository;
import org.kiru.alarm_service.amazonsqs.AwsSqsNotificationSender;
import org.kiru.core.devicetoken.domain.Device;
import org.kiru.core.devicetoken.entity.DeviceJpaEntity;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final AwsSqsNotificationSender awsSqsNotificationSender;

    public Long createDeviceToken(Device device) {
        // userId와 deviceToken을 저장
        log.info("deviceToken : {}", device);
        return alarmRepository.save(DeviceJpaEntity.of(device)).getId();
    }

    private String getDeviceToken(Long recieverId) {
        // recieverId에 해당하는 DeviceToken을 조회
        return alarmRepository.findByUserId(recieverId).getDeviceToken();
    }


}
