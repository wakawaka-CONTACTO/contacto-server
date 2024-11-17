package org.kiru.chat.adapter.in.web.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.kiru.core.chat.chatroom.domain.ChatRoomType;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChatRoomRequest{
    private String title;
    private ChatRoomType chatRoomType;
    private Long userId;
    private Long userId2;

    public static CreateChatRoomRequest of(String title, ChatRoomType chatRoomType, Long userId, Long userId2) {
        return CreateChatRoomRequest.builder()
                .title(title)
                .chatRoomType(chatRoomType)
                .userId(userId)
                .userId2(userId2).build();
    }
}
