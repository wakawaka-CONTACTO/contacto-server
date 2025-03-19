package org.kiru.user.amazonsqs;

public interface NotificationSender {
    NotificationSendResult sendNotification(Notification notification);
}
