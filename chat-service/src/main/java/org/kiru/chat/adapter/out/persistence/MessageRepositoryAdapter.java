package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.GetMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryAdapter implements SaveMessagePort , GetMessageByRoomQuery {
    private final MessageRepository messageRepository;

    @Override
    public Message save(Message message) {
        return MessageJpaEntity.fromEntity(messageRepository.save(MessageJpaEntity.withId(message)));
    }

    @Override
    public List<Message> saveAll(List<MessageJpaEntity> messages) {
       return messageRepository.saveAll(messages)
                .stream().map(MessageJpaEntity::fromEntity).toList();
    }

    @Transactional
    public List<Message> findAllByChatRoomIdWithMessageToRead(Long chatRoomId, Long userId, Boolean isUserAdmin) {
        return messageRepository.findAllByChatRoomIdOrderByCreatedAt(chatRoomId).stream()
                .map(messageJpaEntity -> {
                    if (!messageJpaEntity.getSenderId().equals(userId) & isUserAdmin.equals(false)) {
                        messageJpaEntity.setReadStatus(true);
                    }
                    return MessageJpaEntity.fromEntity(messageJpaEntity);
                }).toList();
    }

    @Override
    public Slice<Message> getMessages(Long roomId, Long userId,Boolean isUserAdmin, Pageable pageable) {
        return messageRepository.findAllByChatRoomIdOrderByCreatedAt(roomId, pageable).map(messageJpaEntity -> {
            if (!messageJpaEntity.getSenderId().equals(userId) && !isUserAdmin) {
                messageJpaEntity.setReadStatus(true);
            }
            return MessageJpaEntity.fromEntity(messageJpaEntity);
        });
    }
}
