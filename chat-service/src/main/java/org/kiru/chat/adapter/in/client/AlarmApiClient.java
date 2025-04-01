package org.kiru.chat.adapter.in.client;

import org.kiru.chat.adapter.in.dto.AlarmMessageRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "alarm-service", url = "${alarm-service.url}")
public interface AlarmApiClient {
    
    @PostMapping("/api/v1/alarm/send/message/user")
    String sendMessageToUser(
        @RequestParam("userId") Long userId,
        @RequestBody AlarmMessageRequest message
    );
} 