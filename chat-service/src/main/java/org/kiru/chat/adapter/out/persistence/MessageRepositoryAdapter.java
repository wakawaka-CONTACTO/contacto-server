package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.GetAllMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements SaveMessagePort , GetAllMessageByRoomQuery {
    private final MessageRepository messageRepository;

    @Override
    public Message save(Message message) {
        MessageJpaEntity entity = MessageJpaEntity.of(message);
        MessageJpaEntity savedEntity = messageRepository.save(entity);
        return MessageJpaEntity.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public List<Message> findAllByChatRoomId(Long chatRoomId, Long userId) {
        return messageRepository.findAllByChatRoomIdOrderByCreatedAt(chatRoomId).stream()
                .map(messageJpaEntity -> {
                    if (!messageJpaEntity.getSenderId().equals(userId)) {
                        messageJpaEntity.setReadStatus(true);
                    }
                    return MessageJpaEntity.fromEntity(messageJpaEntity);
                }).toList();
    }
}
