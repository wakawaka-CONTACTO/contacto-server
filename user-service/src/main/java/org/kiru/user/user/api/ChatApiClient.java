package org.kiru.user.user.api;

import java.util.List;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.config.FeignConfig;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "chat-service",configuration = FeignConfig.class)
public interface ChatApiClient {
    @GetMapping("/api/v1/chat/rooms")
    List<ChatRoom> getUserChatRooms(@RequestHeader("X-User-Id") Long userId, @RequestParam Pageable pageable);

    @GetMapping("/api/v1/chat/rooms/{roomId}")
    ChatRoom getRoom(@PathVariable("roomId") Long roomId, @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/api/v1/chat/rooms")
    CreateChatRoomResponse createRoom(@RequestHeader("X-User-Id") Long userId,
                                      @RequestBody CreateChatRoomRequest createChatRoomRequest);

    @GetMapping("/api/v1/chat/me/rooms")
    List<Long> getAlreadyLikedUserIds(@RequestHeader("X-User-Id") Long userId);

    @GetMapping("/api/v1/chat/connect-user")
    List<Long> getConnectedUserIds();

    @GetMapping("/api/v1/chat/me/matched")
    List<MatchedUserResponse> getMatchedUsers(@RequestHeader("X-User-Id") Long userId);

    @GetMapping("/api/v1/chat/rooms/{roomId}")
    ChatRoom adminGetChatRoom(@PathVariable Long roomId, @RequestHeader("X-User-Id") Long userId, @RequestParam("changeStatus") Boolean changeStatus);

    @GetMapping("/api/v1/chat/cs/rooms")
    ChatRoom getOrCreateCsChatRoom(@RequestParam Long adminId, @RequestParam Long userId);

    @GetMapping("/api/v1/chat/rooms/{roomId}/messages")
    List<Message> getMessages(@PathVariable Long roomId, @RequestHeader("X-User-Id") Long userId,@RequestParam Boolean admin, @RequestParam Pageable pageable);

//    @GetMapping("/api/v1/chat/rooms/{roomId}/message")
//    Slice<Message> getMessagesByAdmin(@PathVariable Long roomId, @RequestHeader("X-User-Id") Long userId, @RequestParam Pageable pageable);
}
