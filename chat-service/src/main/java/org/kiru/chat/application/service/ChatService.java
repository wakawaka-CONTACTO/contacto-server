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
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.port.out.GetAllMessageByRoomQuery;
import org.kiru.chat.application.port.out.GetAlreadyLikedUserIdsQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService implements SendMessageUseCase, CreateRoomUseCase, GetChatRoomUseCase , AddParticipantUseCase ,
        GetAlreadyLikedUserIdsUseCase{
    private final GetChatRoomQuery getChatRoomQuery;
    private final SaveChatRoomPort saveChatRoomPort;
    private final SaveMessagePort saveMessagePort;
    private final GetAllMessageByRoomQuery getAllMessageByRoomQuery;
    private final GetOtherParticipantQuery getOtherParticipantQuery;
    private final GetAlreadyLikedUserIdsQuery getAlreadyLikedUserIdsQuery;

    public ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest) {
            ChatRoom chatRoom = ChatRoom.of(createChatRoomRequest.getTitle(), createChatRoomRequest.getChatRoomType());
            return saveChatRoomPort.save(chatRoom, createChatRoomRequest.getUserId(), createChatRoomRequest.getUserId2());
    }

    public ChatRoom findRoomById(Long roomId, Long userId) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId).orElseThrow(
                RuntimeException::new);
        List<Message> messages = getAllMessageByRoomQuery.findAllByChatRoomId(roomId,userId);
        chatRoom.addMessage(messages);
        Long otherUserId = getOtherParticipantQuery.getOtherParticipantId(roomId, userId);
        chatRoom.addParticipant(otherUserId);
        return chatRoom;
    }

    @Override
    public List<ChatRoom> findRoomsByUserId(Long userId) {
        return getChatRoomQuery.findRoomsByUserId(userId);
    }

    @Transactional
    public Message sendMessage(Long roomId, Message message) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        message.chatRoom(roomId);
        Objects.requireNonNull(chatRoom.getMessages()).add(message);
        saveMessagePort.save(message);
        return message;
    }

    @Override
    public boolean addParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return chatRoom.addParticipant(userId);
    }

    @Override
    public List<Long> getAlreadyLikedUserIds(Long userId) {
        return getAlreadyLikedUserIdsQuery.getAlreadyLikedUserIds(userId);
    }

    @Override
    public List<AdminUserResponse> getMatchedUsers(Long userId) {
        return getAlreadyLikedUserIdsQuery.getMatchedUsers(userId);
    }
}
