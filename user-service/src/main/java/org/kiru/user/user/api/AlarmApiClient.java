package org.kiru.user.user.api;

import org.kiru.user.config.FeignConfig;
import org.kiru.user.user.dto.request.CreatedDeviceReq;
import org.kiru.user.user.dto.response.CreatedDeviceRes;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "alarm-service",configuration = FeignConfig.class)
public interface AlarmApiClient {
    @PostMapping("/api/v1/alarm/device")
    CreatedDeviceRes createDevice(@RequestBody CreatedDeviceReq req);
}
