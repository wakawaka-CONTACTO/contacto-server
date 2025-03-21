package org.kiru.alarm_service.alarm.dto.req;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreatedDeviceTokenReq{
    Long userId;
    String deviceToken;
}
