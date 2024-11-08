package org.kiru.user.user.api;

import java.util.List;
import org.kiru.core.chatroom.domain.ChatRoom;
import org.kiru.user.userlike.api.CreateChatRoomRequest;
import org.kiru.user.userlike.api.CreateChatRoomResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "chat-service")
public interface ChatApiClient {
    @GetMapping("/api/v1/chat/")
    List<ChatRoom> getUserChatRooms(@RequestHeader("X-User-Id") Long userId);

    @GetMapping("/api/v1/chat/rooms/{roomId}")
    ChatRoom getRoom(@PathVariable Long roomId, @RequestHeader("X-User-Id") Long userId);

    @PostMapping("/api/v1/chat/rooms")
    CreateChatRoomResponse createRoom(@RequestHeader("X-User-Id") Long userId,
                                      @RequestBody CreateChatRoomRequest createChatRoomRequest);
}
