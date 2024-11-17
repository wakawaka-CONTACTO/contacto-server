package org.kiru.user.user.dto.response;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

@Builder
public record ChatRoomListResponse(@NotNull Long id, @NotNull String title, @Nullable Set<Long> participants,
                                   String chatRoomThumbnail, int unreadMessageCount, String latestMessageContent) {
    public static ChatRoomListResponse of(ChatRoom chatRoom) {
        return ChatRoomListResponse.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .participants(chatRoom.getParticipants())
                .chatRoomThumbnail(chatRoom.getChatRoomThumbnail())
                .unreadMessageCount(chatRoom.getUnreadMessageCount())
                .latestMessageContent(chatRoom.getLatestMessageContent())
                .build();
    }
}