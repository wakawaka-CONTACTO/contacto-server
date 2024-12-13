package org.kiru.chat.adapter.out.persistence;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomRepositoryAdapter implements GetChatRoomQuery, SaveChatRoomPort, GetOtherParticipantQuery,
        GetAlreadyLikedUserIdsQuery {
    private final ChatRoomRepository chatRoomRepository;
    private final UserJoinChatRoomRepository userJoinChatRoomRepository;

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

    public Optional<ChatRoom> findById(Long id, Boolean isUserAdmin) {
        if (isUserAdmin != null && isUserAdmin) {
            return chatRoomRepository.findById(id)
                    .map(ChatRoom::fromEntity);
        }
        return chatRoomRepository.findById(id)
                .filter(ChatRoomJpaEntity::getVisible)
                .map(ChatRoom::fromEntity);
    }

    public List<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable) {
        Slice<Object[]> results = userJoinChatRoomRepository.findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessageAndParticipants(
                userId, pageable);
        return results.stream().map(result -> {
            ChatRoomJpaEntity chatRoomJpa = (ChatRoomJpaEntity) result[0];
            ChatRoom chatRoom = ChatRoom.fromEntity(chatRoomJpa);
            int unreadMessageCount = ((Number) result[1]).intValue();
            String latestMessageContent = (String) result[2];
            List<Long> participants = Arrays.stream(((String) result[3]).split(","))
                    .map(Long::parseLong)
                    .filter(participantId -> !participantId.equals(userId))
                    .toList();
            chatRoom.addParticipants(participants);
            chatRoom.setUnreadMessageCount(unreadMessageCount);
            chatRoom.setLatestMessageContent(latestMessageContent);
            return chatRoom;
        }).toList();
    }

    public ChatRoom getOrCreateRoom(Long userId, Long adminId) {
        List<UserJoinChatRoom> userChatRoomExist = userJoinChatRoomRepository.findByUserIdAndAdminId(userId, adminId);
        if (!userChatRoomExist.isEmpty()) {
            return chatRoomRepository.findById(userChatRoomExist.getFirst().getChatRoomId())
                    .map(ChatRoom::fromEntity)
                    .orElseThrow(() -> new IllegalStateException("Chat room not found"));
        } else {
            ChatRoom chatRoom = ChatRoom.of("CONTACTO MANAGER", ChatRoomType.PRIVATE);
            ChatRoomJpaEntity chatRoomJpa = chatRoomRepository.saveAndFlush(ChatRoomJpaEntity.of(chatRoom, false));
            userJoinChatRoomRepository.saveAll(
                    List.of(
                            UserJoinChatRoom.builder()
                                    .chatRoomId(chatRoomJpa.getId())
                                    .userId(adminId)
                                    .build(),
                            UserJoinChatRoom.builder()
                                    .chatRoomId(chatRoomJpa.getId())
                                    .userId(userId)
                                    .build()
                    )
            );
            return ChatRoom.fromEntity(chatRoomJpa);
        }
    }

    @Override
    public ChatRoom findAndSetVisible(Long roomId) {
        ChatRoomJpaEntity chatRoomJpaEntity = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        chatRoomJpaEntity.setVisible(true);
        return ChatRoom.fromEntity(chatRoomJpaEntity);
    }

    public ChatRoom findRoomWithMessagesAndParticipants(Long roomId, Long userId, Boolean isUserAdmin) {
        List<Object[]> results = !isUserAdmin ?
                chatRoomRepository.findRoomWithMessagesAndParticipants(roomId)
                        .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND))
                : chatRoomRepository.findRoomWithMessagesAndParticipantsByAdmin(roomId)
                        .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        ChatRoom chatRoom = ChatRoom.fromEntity((ChatRoomJpaEntity) results.getFirst()[0]);
        for (Object[] result : results) {
            Optional.ofNullable((MessageJpaEntity) result[1])
                    .map(MessageJpaEntity::fromEntity).ifPresent(chatRoom::addMessage);
            Optional.ofNullable((Long) result[2])
                    .ifPresent(chatRoom::addParticipant);
        }
        return chatRoom;
    }

    public Long getOtherParticipantId(Long roomId, Long senderId) {
        List<Long> otherParticipantIds = userJoinChatRoomRepository.findOtherParticipantIds(roomId, senderId);
        return otherParticipantIds.isEmpty() ? null : otherParticipantIds.getFirst();
    }

    public List<Long> getAlreadyLikedUserIds(Long userId) {
        return userJoinChatRoomRepository.findAlreadyLikedUserIds(userId);
    }

    public List<AdminUserResponse> getMatchedUsers(Long userId) {
        return userJoinChatRoomRepository.getMatchedUser(userId);
    }
}