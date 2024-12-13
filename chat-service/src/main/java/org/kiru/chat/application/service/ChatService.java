package org.kiru.chat.application.service;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.chat.adapter.out.persistence.GetOtherParticipantQuery;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetAlreadyLikedUserIdsUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.port.out.GetMessageByRoomQuery;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest) {
            ChatRoom chatRoom = ChatRoom.of(createChatRoomRequest.getTitle(), createChatRoomRequest.getChatRoomType());
            return saveChatRoomPort.save(chatRoom, createChatRoomRequest.getUserId(), createChatRoomRequest.getUserId2());
    }


    public ChatRoom findRoomById(Long roomId, Long userId, Boolean isUserAdmin) {
        ChatRoom chatRoom = getChatRoomQuery.findRoomWithMessagesAndParticipants(roomId, userId, isUserAdmin);
        if (isUserAdmin || !chatRoom.getParticipants().contains(userId)) {
            throw new ForbiddenException(FailureCode.CHAT_ROOM_ACCESS_DENIED);
        }
        chatRoom.removeParticipant(userId);
        return chatRoom;
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

    @Transactional
    public Message sendMessage(Long roomId, Message message) {
        ChatRoom chatRoom = getChatRoomQuery.findAndSetVisible(roomId);
        if (chatRoom == null) {
            throw new EntityNotFoundException(FailureCode.CHATROOM_NOT_FOUND);
        }
        try {
            message.chatRoom(roomId);
            Objects.requireNonNull(chatRoom.getMessages()).add(message);
            saveMessagePort.save(message);
            return message;
        } catch (Exception e) {
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
