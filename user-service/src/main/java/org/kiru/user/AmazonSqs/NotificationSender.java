package org.kiru.user.AmazonSqs;

public interface NotificationSender {
    NotificationSendResult sendNotification(Notification notification);
}
