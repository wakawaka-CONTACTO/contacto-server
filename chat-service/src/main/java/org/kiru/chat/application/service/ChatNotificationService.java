package org.kiru.chat.application.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kiru.chat.adapter.in.client.AlarmApiClient;
import org.kiru.chat.adapter.in.client.UserApiClient;
import org.kiru.chat.adapter.in.dto.AlarmMessageRequest;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.stereotype.Service;

import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationService {
    
    private final AlarmApiClient alarmApiClient;
    private final UserApiClient userApiClient;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void sendNotification(Message message) {
        log.info("💬 채팅 알림 전송 시작 - messageId: {}, senderId: {}, chatRoomId: {}", 
            message.getId(), message.getSendedId(), message.getChatRoomId());
        CompletableFuture.runAsync(() -> {
            try {
                log.info("👤 사용자 이름 조회 중 - userId: {}", message.getSenderId());
                String title = userApiClient.getUsername(message.getSenderId());
                String body = message.getContent();
                Map<String, String> content = new HashMap<>();
                content.put("type", "chat");
                content.put("chatRoomId", message.getChatRoomId().toString());
                
                log.info("📢 알림 메시지 구성 - title: {}, body: {}, content: {}", title, body, content);
                alarmApiClient.sendMessageToUser(message.getSendedId(), AlarmMessageRequest.of(title, body, content));
                log.info("✅ 채팅 알림 전송 완료 - messageId: {}", message.getId());
            } catch (EntityNotFoundException e) {
                log.error("❌ 사용자를 찾을 수 없음 - userId: {}, error: {}", message.getSendedId(), e.getMessage());
            } catch (FeignException e) {
                log.error("❌ 사용자 서비스 통신 실패 - userId: {}, status: {}, message: {}", 
                    message.getSendedId(), e.status(), e.contentUTF8());
            } catch (Exception e) {
                log.error("❌ 채팅 알림 전송 실패 - messageId: {}, error: {}", message.getId(), e.getMessage(), e);
            }
        }, virtualThreadExecutor);
    }
} 