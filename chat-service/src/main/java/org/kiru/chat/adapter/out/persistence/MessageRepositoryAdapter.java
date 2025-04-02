package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.GetMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.common.PageableResponse;
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
        return MessageJpaEntity.toModel(messageRepository.save(MessageJpaEntity.withId(message)));
    }

    public List<Message> saveAll(List<MessageJpaEntity> messages) {
       return messageRepository.saveAll(messages)
                .stream().map(MessageJpaEntity::toModel).toList();
    }

    @Transactional
    public List<Message> findAllByChatRoomIdWithMessageToRead(Long chatRoomId, Long userId, Boolean isUserAdmin) {
        return messageRepository.findAllByChatRoomIdOrderByCreatedAt(chatRoomId).stream()
                .map(messageJpaEntity -> {
                    // 읽음 처리하지 않고 그대로 반환
                    return MessageJpaEntity.toModel(messageJpaEntity);
                }).toList();
    }

    @Override
    public PageableResponse<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin, Pageable pageable) {
        Slice<MessageJpaEntity> messageSlice = messageRepository.findAllByChatRoomId(roomId, pageable);
        List<Message> messages = messageSlice.getContent().stream().parallel().map(messageJpaEntity -> {
                    // 읽음 처리하지 않고 그대로 반환
                    return MessageJpaEntity.toModel(messageJpaEntity);
                })
                .toList();
        return PageableResponse.of(messageSlice, messages);
    }
}
