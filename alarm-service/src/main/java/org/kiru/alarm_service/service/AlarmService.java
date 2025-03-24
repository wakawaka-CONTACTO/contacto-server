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

    public void sendMatchingAlarm (Long recieverId) {
        //  recieverId에 DevioceToken 조화
        String deviceToken = getDeviceToken(recieverId);
        // deviceToken을 이용하여 알람 전송
        // SQS를 위한 메세지 만들기
        PushMessage message = PushMessage.builder()
                .title("알림 제목 예시")
                .body("푸시 알림 내용 예시입니다.")
                .deviceToken(deviceToken)
                .data(Map.of("data", "데이터 예시",
                        "data2", "데이터2 예시"))
                .build();
        awsSqsNotificationSender.sendPushMessage(message);
        // SQS로 메세지 전송
    }

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
