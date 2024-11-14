package org.kiru.chat.application.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.chat.adapter.in.web.CreateChatRoomRequest;
import org.kiru.chat.application.port.in.AddParticipantUseCase;
import org.kiru.chat.application.port.in.CreateRoomUseCase;
import org.kiru.chat.application.port.in.GetChatRoomUseCase;
import org.kiru.chat.application.port.in.SendMessageUseCase;
import org.kiru.chat.application.port.out.GetAllMessageByRoomQuery;
import org.kiru.chat.application.port.out.GetChatRoomQuery;
import org.kiru.chat.application.port.out.SaveChatRoomPort;
import org.kiru.chat.application.port.out.SaveMessagePort;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService implements SendMessageUseCase, CreateRoomUseCase, GetChatRoomUseCase , AddParticipantUseCase {
    private final GetChatRoomQuery getChatRoomQuery;
    private final SaveChatRoomPort saveChatRoomPort;
    private final SaveMessagePort saveMessagePort;
    private final GetAllMessageByRoomQuery getAllMessageByRoomQuery;

    public ChatRoom createRoom(CreateChatRoomRequest createChatRoomRequest) {
            ChatRoom chatRoom = ChatRoom.of(createChatRoomRequest.getTitle(), createChatRoomRequest.getChatRoomType());
            return saveChatRoomPort.save(chatRoom, createChatRoomRequest.getUserId(), createChatRoomRequest.getUserId2());
    }

    public ChatRoom findRoomById(Long roomId, Long userId) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId).orElseThrow(
                RuntimeException::new);
        List<Message> messages = getAllMessageByRoomQuery.findAllByChatRoomId(roomId);
        chatRoom.addMessage(messages);
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
        chatRoom.getMessages().add(message);
        saveMessagePort.save(message);
        return message;
    }

    @Override
    public boolean addParticipant(Long roomId, Long userId) {
        ChatRoom chatRoom = getChatRoomQuery.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        return chatRoom.addParticipant(userId);
    }
}
