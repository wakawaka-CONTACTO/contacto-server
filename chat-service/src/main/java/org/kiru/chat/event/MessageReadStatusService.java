package org.kiru.chat.event;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.out.persistence.MessageRepository;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageReadStatusService {
    private final MessageRepository messageRepository;

    @Transactional
    public void markAllMessagesAsRead(Long chatRoomId, Long userId) {
        List<MessageJpaEntity> messages = messageRepository.findAllByChatRoomIdAndReadStatusFalse(chatRoomId);
        for (MessageJpaEntity message : messages) {
            message.setReadStatus(true);
            messageRepository.save(message);
        }
    }
}
