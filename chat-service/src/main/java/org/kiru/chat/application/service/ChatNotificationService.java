package org.kiru.chat.application.service;

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
        CompletableFuture.runAsync(() -> {
            try {
                String title = userApiClient.getUsername(message.getSendedId());
                String body = message.getContent();
                alarmApiClient.sendMessageToUser(message.getSendedId(), AlarmMessageRequest.of(title, body));
            } catch (EntityNotFoundException e) {
                log.error("User not found for chat notification: {}", message.getSendedId());
            } catch (FeignException e) {
                log.error("Failed to communicate with user-service for chat notification: {} - Status: {}, Message: {}", 
                    message.getSendedId(), e.status(), e.contentUTF8());
            } catch (Exception e) {
                log.error("Failed to send chat notification to user: {}", message.getSendedId(), e);
            }
        }, virtualThreadExecutor);
    }
} 