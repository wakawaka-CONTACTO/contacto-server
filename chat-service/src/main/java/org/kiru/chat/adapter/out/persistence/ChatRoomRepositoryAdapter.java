package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryAdapter implements GetChatRoomQuery, SaveChatRoomPort, GetOtherParticipantQuery {
    private final ChatRoomRepository chatRoomRepository;
    private final UserJoinChatRoomRepository userJoinChatRoomRepository;

    @Override
    @Transactional
    public ChatRoom save(ChatRoom chatRoom, Long userId, Long userId2) {
        ChatRoomJpaEntity entity = ChatRoomJpaEntity.of(chatRoom);
        ChatRoomJpaEntity chatRoomJpa = chatRoomRepository.save(entity);
        UserJoinChatRoom userFirst = UserJoinChatRoom.builder()
                .chatRoomId(chatRoomJpa.getId())
                .userId(userId)
                .build();
        UserJoinChatRoom userSecond = UserJoinChatRoom.builder()
                .chatRoomId(chatRoomJpa.getId())
                .userId(userId2)
                .build();
        userJoinChatRoomRepository.saveAll(List.of(userFirst, userSecond));
        return ChatRoom.fromEntity(chatRoomJpa);
    }

    @Override
    @Transactional
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomRepository.findById(id)
                .map(ChatRoom::fromEntity);
    }

    @Override
    public List<ChatRoom> findRoomsByUserId(Long userId) {
        List<Object[]> results = userJoinChatRoomRepository.findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessage(userId);
        return results.stream().map(result -> {
            ChatRoomJpaEntity chatRoomJpa = (ChatRoomJpaEntity) result[0];
            int unreadMessageCount = ((Number) result[1]).intValue();
            String latestMessageContent = (String) result[2];
            ChatRoom chatRoom = ChatRoom.fromEntity(chatRoomJpa);
            chatRoom.setUnreadMessageCount(unreadMessageCount);
            chatRoom.setLatestMessageContent(latestMessageContent);
            return chatRoom;
        }).toList();
    }

    @Override
    public Long getOtherParticipantId(Long roomId, Long senderId) {
        List<Long> otherParticipantIds = userJoinChatRoomRepository.findOtherParticipantIds(roomId, senderId);
        return otherParticipantIds.isEmpty() ? null : otherParticipantIds.get(0);
    }
}