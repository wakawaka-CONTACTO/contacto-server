package org.kiru.chat.application.port.out;

import org.kiru.core.chat.message.domain.Message;

public interface SendMessagePort {
    void sendMessage(Message message, String receiverId);

    void sendMessageAck(String recordId);
}
