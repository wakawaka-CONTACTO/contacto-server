package org.kiru.chat.application.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.chat.adapter.out.persistence.GetOtherParticipantQuery;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetAlreadyLikedUserIdsUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.GetMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.chat.event.MessageCreateEvent;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.chat.message.domain.TranslateLanguage;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements SendMessageUseCase, CreateRoomUseCase, GetChatRoomUseCase , AddParticipantUseCase ,
        GetAlreadyLikedUserIdsUseCase, GetMessageUseCase {
    private final GetChatRoomQuery getChatRoomQuery;
    private final SaveChatRoomPort saveChatRoomPort;
    private final SaveMessagePort saveMessagePort;
    private final GetMessageByRoomQuery getMessageByRoomQuery;
    private final GetOtherParticipantQuery getOtherParticipantQuery;
    private final GetAlreadyLikedUserIdsQuery getAlreadyLikedUserIdsQuery;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Creates a new chat room based on the provided request details.
     *
     * @param createChatRoomRequest the request containing details for creating a chat room
     * @return the newly created and saved ChatRoom instance
     * @throws IllegalArgumentException if the request contains invalid room creation parameters
     */
    public ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest) {
            ChatRoom chatRoom = ChatRoom.of(createChatRoomRequest.getTitle(), createChatRoomRequest.getChatRoomType());
            return saveChatRoomPort.save(chatRoom, createChatRoomRequest.getUserId(), createChatRoomRequest.getUserId2());
    }

    public ChatRoom findRoomById(Long roomId, Long userId, boolean isUserAdmin) {
        ChatRoom chatRoom = getChatRoomQuery.findRoomWithMessagesAndParticipants(roomId, userId, isUserAdmin);
        if (isUserAdmin || Objects.requireNonNull(chatRoom.getParticipants()).contains(userId)) {
            chatRoom.removeParticipant(userId);
            return chatRoom;
        }
        throw new ForbiddenException(FailureCode.CHAT_ROOM_ACCESS_DENIED);
    }

    @Override
    public List<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable) {
        return getChatRoomQuery.findRoomsByUserId(userId,pageable);
    }

    @Override
    public ChatRoom getOrCreateRoomUseCase(Long userId, Long adminId) {
        ChatRoom chatRoom = getChatRoomQuery.getOrCreateRoom(userId, adminId);
        Long otherUserId = getOtherParticipantQuery.getOtherParticipantId(chatRoom.getId(), userId);
        if (otherUserId != null) {
            chatRoom.addParticipant(otherUserId);
            List<Message> messages = getMessageByRoomQuery.findAllByChatRoomId(chatRoom.getId(), userId, true);
            chatRoom.addMessage(messages);
            return chatRoom;
        }
        return chatRoom;
    }

    /**
     * Sends a message to a specified chat room with optional translation and event publishing.
     *
     * @param roomId The unique identifier of the chat room where the message will be sent
     * @param message The message to be sent, containing content and sender information
     * @param isUserConnected A flag indicating whether the user is currently connected
     * @param translateLanguage The language for potential message translation (can be null)
     * @return The saved message after being persisted in the system
     * @throws EntityNotFoundException If the specified chat room cannot be found
     * @throws ForbiddenException If message sending fails due to system constraints
     */
    @Transactional
    public Message sendMessage(final Long roomId,Message message,final boolean isUserConnected,
                               final TranslateLanguage translateLanguage) {
        ChatRoom chatRoom = getChatRoomQuery.findAndSetVisible(roomId);
        if (chatRoom == null) {
            throw new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND);
        }
        try {
            message.chatRoom(roomId);
            Objects.requireNonNull(chatRoom.getMessages()).add(message);
            Message saveMessage = saveMessagePort.save(message);
            if(isUserConnected && translateLanguage != null && message.getSendedId() != null) {
                applicationEventPublisher.publishEvent(MessageCreateEvent.of(saveMessage.getId(),message.getSendedId().toString(),message.getContent()));
            }
            return saveMessage;
        } catch (Exception e) {
            log.error("Failed to send message", e);
            throw new ForbiddenException(FailureCode.CHAT_MESSAGE_SEND_FAILED);
        }
    }

    @Override
    public boolean addParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId, false)
                .orElseThrow(() -> new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND));
        if (!chatRoom.addParticipant(userId)) {
            throw new ForbiddenException(FailureCode.CHAT_ROOM_JOIN_FAILED);
        }
        return true;
    }

    @Override
    public List<Long> getAlreadyLikedUserIds(Long userId) {
        return getAlreadyLikedUserIdsQuery.getAlreadyLikedUserIds(userId);
    }

    @Override
    public List<AdminUserResponse> getMatchedUsers(Long userId) {
        return getAlreadyLikedUserIdsQuery.getMatchedUsers(userId);
    }

    @Override
    public Slice<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin,Pageable pageable) {
        return getMessageByRoomQuery.getMessages(roomId, userId,isUserAdmin, pageable);
    }
}
