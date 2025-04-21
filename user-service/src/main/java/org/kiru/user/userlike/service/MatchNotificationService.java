package org.kiru.user.userlike.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.kiru.user.user.api.AlarmApiClient;
import org.kiru.user.userlike.api.AlarmMessageRequest;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchNotificationService {
    private final AlarmApiClient alarmApiClient;
    private final Random random = new Random();

    private static final List<MatchNotification> MATCH_NOTIFICATIONS = List.of(
        new MatchNotification(
            "You've been matched! 🎉",
            "Your match is waiting! Start chat and get to know each other! 💬"
        ),
        new MatchNotification(
            "You've been matched! 🎉",
            "Don't be shy—send a message and make a great first impression! 🙉"
        ),
        new MatchNotification(
            "You've been matched! 🎉",
            "Send them a message and break the ice! ❄️ Say hello now!"
        ),
        new MatchNotification(
            "You've been matched! 🎉",
            "Can't wait to see your match? Take a look now! 👀"
        )
    );

    public void sendMatchNotifications(Long userId, Long matchedUserId, Long chatRoomId) {
        log.info("💘 매치 알림 전송 시작 - userId: {}, matchedUserId: {}, chatRoomId: {}", 
            userId, matchedUserId, chatRoomId);
        try {
            MatchNotification notification = getRandomNotification();
            log.info("🎲 선택된 알림 메시지 - title: {}, body: {}", notification.title(), notification.body());
    
            Map<String, String> content = new HashMap<>();
            content.put("type", "chat");
            content.put("chatRoomId", chatRoomId.toString());
            
            log.info("📢 첫 번째 사용자에게 알림 전송 - userId: {}", userId);
            alarmApiClient.sendMessageToUser(userId, 
                AlarmMessageRequest.of(notification.title(), notification.body(), content));
            log.info("✅ 첫 번째 사용자 알림 전송 완료 - userId: {}", userId);
            
            log.info("📢 두 번째 사용자에게 알림 전송 - userId: {}", matchedUserId);
            alarmApiClient.sendMessageToUser(matchedUserId, 
                AlarmMessageRequest.of(notification.title(), notification.body(), content));
            log.info("✅ 두 번째 사용자 알림 전송 완료 - userId: {}", matchedUserId);
            
            log.info("✨ 매치 알림 전송 완료 - userId: {}, matchedUserId: {}", userId, matchedUserId);
        } catch (Exception e) {
            log.error("❌ 매치 알림 전송 실패 - userId: {}, matchedUserId: {}, error: {}", 
                userId, matchedUserId, e.getMessage(), e);
        }
    }

    private MatchNotification getRandomNotification() {
        return MATCH_NOTIFICATIONS.get(random.nextInt(MATCH_NOTIFICATIONS.size()));
    }

    private record MatchNotification(String title, String body) {}
} 