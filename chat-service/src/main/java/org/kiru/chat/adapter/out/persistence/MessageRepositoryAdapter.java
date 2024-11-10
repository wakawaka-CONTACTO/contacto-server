package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.GetAllMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.stereotype.Repository;

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
    public List<Message> findAllByChatRoomId(Long chatRoomId) {
        return messageRepository.findAllByChatRoomIdOrderByCreatedAt(chatRoomId).stream()
                .map(MessageJpaEntity::fromEntity).toList();
    }
}
