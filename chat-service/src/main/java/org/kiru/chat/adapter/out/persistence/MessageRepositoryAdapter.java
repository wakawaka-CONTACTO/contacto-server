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

    /**
     * Saves a message to the repository and returns the saved message.
     *
     * @param message The message to be saved
     * @return The saved message with potentially updated metadata (e.g., generated ID)
     */
    @Override
    public Message save(Message message) {
        return MessageJpaEntity.fromEntity(messageRepository.save(MessageJpaEntity.of(message)));
    }

    /**
     * Retrieves all messages for a specific chat room and updates their read status.
     *
     * @param chatRoomId The unique identifier of the chat room
     * @param userId The identifier of the current user
     * @param isUserAdmin A flag indicating whether the user has admin privileges
     * @return A list of messages from the specified chat room, with read status potentially updated
     *
     * @throws IllegalArgumentException if chatRoomId is null
     *
     * @see MessageRepository
     */
    @Override
    @Transactional
    public List<Message> findAllByChatRoomId(Long chatRoomId, Long userId, Boolean isUserAdmin) {
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
