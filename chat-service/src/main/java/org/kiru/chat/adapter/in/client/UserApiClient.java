package org.kiru.chat.adapter.in.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserApiClient {
    
    @GetMapping("/api/v1/users/{userId}/username")
    String getUsername(@PathVariable("userId") Long userId);
} 