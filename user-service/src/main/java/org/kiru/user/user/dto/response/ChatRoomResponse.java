package org.kiru.user.user.dto.response;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.springframework.data.domain.Slice;

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

    private Slice<MessageResponse> slicedMessages;

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

    public static ChatRoomResponse of(ChatRoom chatRoom, Slice<MessageResponse> messageResponses){
        return ChatRoomResponse.builder()
            .id(chatRoom.getId())
            .title(chatRoom.getTitle())
            .messages(null)
            .slicedMessages(messageResponses)
            .participants(chatRoom.getParticipants())
            .chatRoomThumbnail(chatRoom.getChatRoomThumbnail())
            .build();
    }
}