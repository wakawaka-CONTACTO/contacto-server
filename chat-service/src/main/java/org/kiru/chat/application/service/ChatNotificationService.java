package org.kiru.chat.application.service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kiru.chat.adapter.in.client.AlarmApiClient;
import org.kiru.chat.adapter.in.dto.AlarmMessageRequest;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatNotificationService {
    
    private final AlarmApiClient alarmApiClient;
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void sendNotification(Long receiverId, Message message) {
        CompletableFuture.runAsync(() -> {
            try {
                String title = "새로운 메시지";
                String body = message.getContent();
                alarmApiClient.sendMessageToUser(receiverId, AlarmMessageRequest.of(title, body));
            } catch (Exception e) {
                log.error("Failed to send chat notification to user: {}", receiverId, e);
            }
        }, virtualThreadExecutor);
    }
} 