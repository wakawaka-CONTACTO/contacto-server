package org.kiru.user.user.api;

import org.kiru.user.config.FeignConfig;
import org.kiru.user.user.dto.request.CreatedDeviceTokenReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "alarm-service",configuration = FeignConfig.class)
public interface AlarmApiClient {
    @PostMapping("/api/v1/alarm/devicetoken")
    Void addDeviceToken(@RequestBody CreatedDeviceTokenReq req);
}
