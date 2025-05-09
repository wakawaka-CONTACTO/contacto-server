package org.kiru.user.user.dto.response;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiru.core.chat.chatroom.domain.ChatRoom;

@Getter
@AllArgsConstructor
@Builder
public class ChatRoomResponse {
    @NotNull
    private Long id;

    @NotNull
    private String title;

    @Nullable
    private List<MessageResponse> messages;

    @Nullable
    private Set<Long> participants;

    @Setter
    private String chatRoomThumbnail;

    public static ChatRoomResponse of(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .title(chatRoom.getTitle())
                .messages(chatRoom.getMessages().stream()
                        .map(MessageResponse::fromMessage)
                        .toList())
                .participants(chatRoom.getParticipants())
                .chatRoomThumbnail(chatRoom.getChatRoomThumbnail())
                .build();
    }
}