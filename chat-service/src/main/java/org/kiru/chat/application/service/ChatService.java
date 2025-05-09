package org.kiru.chat.application.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.chat.adapter.in.web.req.CreateChatRoomRequest;
import org.kiru.chat.adapter.in.web.res.AdminUserResponse;
import org.kiru.core.common.PageableResponse;
import org.kiru.chat.adapter.out.persistence.GetOtherParticipantQuery;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetAlreadyLikedUserIdsUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.GetMessageUseCase;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.GetMessageByRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.chat.event.MessageReadStatusService;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService implements CreateRoomUseCase, GetChatRoomUseCase , AddParticipantUseCase ,
        GetAlreadyLikedUserIdsUseCase, GetMessageUseCase {
    private final GetChatRoomQuery getChatRoomQuery;
    private final SaveChatRoomPort saveChatRoomPort;
    private final GetMessageByRoomQuery getMessageByRoomQuery;
    private final GetOtherParticipantQuery getOtherParticipantQuery;
    private final GetAlreadyLikedUserIdsQuery getAlreadyLikedUserIdsQuery;
    private final MessageReadStatusService messageReadStatusService;

    public ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest) {
            ChatRoom chatRoom = ChatRoom.of(createChatRoomRequest.getTitle(), createChatRoomRequest.getChatRoomType());
            return saveChatRoomPort.save(chatRoom, createChatRoomRequest.getUserId(), createChatRoomRequest.getUserId2());
    }

    public ChatRoom findRoomById(Long roomId, Long userId, boolean isUserAdmin) {
        return getChatRoomQuery.findRoomWithMessagesAndParticipants(roomId, userId, isUserAdmin);
    }

    @Override
    public PageableResponse<ChatRoom> findRoomsByUserId(Long userId, Pageable pageable) {
        PageableResponse<ChatRoom> chatRooms = getChatRoomQuery.findRoomsByUserId(userId, pageable);
        
        // 각 채팅방의 읽지 않은 메시지 수를 정확히 업데이트
        chatRooms.getContent().forEach(chatRoom -> {
            int unreadCount = messageReadStatusService.getUnreadMessageCount(chatRoom.getId(), userId);
            chatRoom.setUnreadMessageCount(unreadCount);
        });
        
        return chatRooms;
    }

    @Override
    public ChatRoom getOrCreateRoomUseCase(Long userId, Long adminId) {
        ChatRoom chatRoom = getChatRoomQuery.getOrCreateRoom(userId, adminId);
        Long otherUserId = getOtherParticipantQuery.getOtherParticipantId(chatRoom.getId(), userId);
        if (otherUserId != null) {
            chatRoom.addParticipant(otherUserId);
            List<Message> messages = getMessageByRoomQuery.findAllByChatRoomIdWithMessageToRead(chatRoom.getId(), userId, true);
            chatRoom.addMessage(messages);
            return chatRoom;
        }
        return chatRoom;
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
    public PageableResponse<Message> getMessages(Long roomId, Long userId, Boolean isUserAdmin,Pageable pageable) {
        return getMessageByRoomQuery.getMessages(roomId, userId,isUserAdmin, pageable);
    }
}
