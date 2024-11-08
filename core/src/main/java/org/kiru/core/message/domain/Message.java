package org.kiru.core.message.domain;
import jakarta.annotation.Nonnull;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@Builder
@AllArgsConstructor
public class Message {
    private Long id;
    @Nonnull
    private String content;
    @Nonnull
    private Long senderId;
    @Nonnull
    private Long chatRoomId;

    private LocalDateTime createdAt;

    public void chatRoom(Long chatRoomId){
        this.chatRoomId = chatRoomId;
    }
    public static Message of(Long id, String content, Long senderId, LocalDateTime createdAt, Long chatRoomId) {
        return Message.builder()
                .id(id)
                .content(content)
                .senderId(senderId)
                .createdAt(createdAt)
                .chatRoomId(chatRoomId)
                .build();
    }
}

