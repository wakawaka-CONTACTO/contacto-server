package org.kiru.user.user.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.chat.message.domain.Message;

@Getter
@AllArgsConstructor
@Builder
public class MessageResponse {
    @NotNull
    private String content;

    @NotNull
    private Long senderId;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    private Boolean readStatus;

    public static MessageResponse fromMessage(Message message) {
        return MessageResponse.builder()
                .content(message.getContent())
                .senderId(message.getSenderId())
                .createdAt(message.getCreatedAt())
                .readStatus(message.getReadStatus())
                .build();
    }
}