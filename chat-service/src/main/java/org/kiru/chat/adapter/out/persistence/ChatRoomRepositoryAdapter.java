package org.kiru.chat.adapter.out.persistence;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.core.common.PageableResponse;
import org.kiru.chat.adapter.out.persistence.dto.ChatRoomProjection;
import org.kiru.chat.adapter.out.persistence.dto.ChatRoomWithDetails;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;
import org.kiru.core.chat.chatroom.entity.ChatRoomJpaEntity;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.entity.MessageJpaEntity;
import org.kiru.core.chat.userchatroom.entity.UserJoinChatRoom;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.cache.annotation.Cacheable;
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
    private final MessageRepository messageRepository;

    @Transactional
    public ChatRoom save(ChatRoom chatRoom, Long userId, Long userId2) {
        Optional<UserJoinChatRoom> alreadyRoom = userJoinChatRoomRepository.findAlreadyRoomByUserIds(userId, userId2);
        if (alreadyRoom.isPresent()) {
            log.info("Chat room already exist for user {} and user {}", userId, userId2);
            return chatRoomRepository.findById(alreadyRoom.get().getChatRoomId())
                    .map(ChatRoomJpaEntity::toModel)
                    .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        }
        log.info("Creating chat room for user {} and user {}", userId, userId2);
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
        return ChatRoomJpaEntity.toModel(chatRoomJpa);
    }

    public Optional<ChatRoom> findById(Long id, boolean isUserAdmin) {
        if (isUserAdmin) {
            return chatRoomRepository.findById(id)
                    .map(ChatRoomJpaEntity::toModel);
        }
        return chatRoomRepository.findById(id)
                .filter(ChatRoomJpaEntity::getVisible)
                .map(ChatRoomJpaEntity::toModel);
    }

    public PageableResponse<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable) {
        Slice<ChatRoomProjection> chatRoomSlice =
                userJoinChatRoomRepository.findChatRoomsByUserIdWithUnreadMessageCountAndLatestMessageAndParticipants(userId, pageable);
        List<ChatRoom> chatRooms = chatRoomSlice.getContent().stream().map(result -> {
            ChatRoom chatRoom = ChatRoomJpaEntity.toModel(result.getChatRoom());
            int unreadMessageCount = result.getUnreadMessageCount();
            String latestMessageContent = result.getLatestMessageContent();
            List<Long> participants = Pattern.compile(",")
                    .splitAsStream(result.getParticipants())
                    .mapToLong(Long::parseLong)
                    .filter(participantId -> participantId != userId)
                    .boxed()
                    .toList();
            chatRoom.addParticipants(participants);
            chatRoom.setUnreadMessageCount(unreadMessageCount);
            chatRoom.setLatestMessageContent(latestMessageContent);
            return chatRoom;
        }).toList();
        return PageableResponse.of(chatRoomSlice, chatRooms);
    }

    @Transactional
    public ChatRoom getOrCreateRoom(Long userId, Long adminId) {
        List<UserJoinChatRoom> userChatRoomExist = userJoinChatRoomRepository.findByUserIdAndAdminId(userId, adminId);
        if (!userChatRoomExist.isEmpty()) {
            return chatRoomRepository.findById(userChatRoomExist.getFirst().getChatRoomId())
                    .map(ChatRoomJpaEntity::toModel)
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
            return ChatRoomJpaEntity.toModel(chatRoomJpa);
        }
    }

    @Override
    @Cacheable(value = "chatRoom", key = "#roomId", unless = "#result == null")
    public ChatRoom findAndSetVisible(Long roomId) {
        ChatRoomJpaEntity chatRoomJpaEntity = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        chatRoomJpaEntity.setVisible(true);
        return ChatRoomJpaEntity.toModel(chatRoomJpaEntity);
    }

    @Transactional
    public ChatRoom findRoomWithMessagesAndParticipants(Long roomId, Long userId, boolean isUserAdmin) {
        List<ChatRoomWithDetails> results = isUserAdmin ?
                chatRoomRepository.findRoomWithMessagesAndParticipantsByAdmin(roomId)
                        .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND)) :
                chatRoomRepository.findRoomWithMessagesAndParticipants(roomId)
                        .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        if (results.isEmpty()) {
            throw new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND);
        }
        return getChatRoomByUserIdAndIsUserAdmin(userId, isUserAdmin, results, ChatRoomJpaEntity.toModel(results.getFirst().chatRoom()));
    }

    private ChatRoom getChatRoomByUserIdAndIsUserAdmin(Long userId, boolean isUserAdmin,
                                                       List<ChatRoomWithDetails> results, ChatRoom chatRoom) {
        List<Long> participants = results.stream()
                .map(ChatRoomWithDetails::userId)
                .toList();
        if(!isUserAdmin && !participants.contains(userId)){
            throw new ForbiddenException(FailureCode.CHAT_ROOM_ACCESS_DENIED);
        }
        chatRoom.addParticipants(participants);
        List<Message> messages;
        if(participants.contains(userId)){
            List<MessageJpaEntity> resultsMessages = results.stream()
                    .map(ChatRoomWithDetails::message)
                    .filter(Objects::nonNull)
                    .map(messageJpaEntity -> {
                                if (!userId.equals(messageJpaEntity.getSenderId())) {
                                    messageJpaEntity.setReadStatus(true);
                                }
                                return messageJpaEntity;
                            }
                    )
                    .distinct()
                    .toList();
            messages = messageRepository.saveAll(resultsMessages).stream().map(MessageJpaEntity::toModel).toList();
            chatRoom.removeParticipant(userId);
        }else{
            messages = results.stream()
                    .map(ChatRoomWithDetails::message)
                    .map(MessageJpaEntity::toModel)
                    .distinct()
                    .filter(Objects::nonNull)
                    .toList();
        }
        chatRoom.addMessage(messages);
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