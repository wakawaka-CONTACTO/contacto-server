package org.kiru.user.amazonsqs;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 알림 메시지
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Notification {
    private String message;
    private LocalDateTime createAt;

    public static Notification create(String message) {
        return new Notification(
                message,
                LocalDateTime.now()
        );
    }
}