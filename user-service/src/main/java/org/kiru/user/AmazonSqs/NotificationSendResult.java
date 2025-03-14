package org.kiru.user.AmazonSqs;

// 알람전송 결과
public record NotificationSendResult(
        String messageId,
        boolean success
) {

    public static NotificationSendResult success(String messageId) {
        return new NotificationSendResult(messageId, true);
    }

    public static NotificationSendResult failure() {
        return new NotificationSendResult(null, false);
    }
}