package org.kiru.core.chat.chatroom.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChatRoom {
    @NotNull
    private Long id;

    @NotNull
    private String title;

    @Nullable
    @Builder.Default
    private final List<Message> messages = new ArrayList<>();

    @NotNull
    private ChatRoomType type;

    @Nullable
    @Builder.Default
    private final Set<Long> participants = new HashSet<>(); // Set으로 변경

    private String chatRoomThumbnail;

    @Setter
    private int unreadMessageCount;

    @Setter
    private String latestMessageContent;

    public void addMessage(final List<Message> message) {
        Objects.requireNonNull(this.messages).addAll(message);
    }

    public boolean addParticipant(final Long userId) {
        if (this.type == ChatRoomType.PRIVATE) {
            return Objects.requireNonNull(this.participants).add(userId);
        }
        return false;
    }

    public void addParticipants(final List<Long> userIds) {
        if (this.type == ChatRoomType.PRIVATE) {
            Objects.requireNonNull(this.participants).addAll(userIds);
        }
    }

    public static ChatRoom of(String title, ChatRoomType type) {
        return ChatRoom.builder()
                .title(title)
                .type(type)
                .messages(new ArrayList<>())
                .participants(new HashSet<>())
                .build();
    }

    public void removeParticipant(Long userId) {
        Objects.requireNonNull(this.participants).remove(userId);
    }

    @JsonIgnore
    public void setThumbnailAndRoomTitle(Map<Long, UserPortfolioItem> userPortfolioImgMap) {
        Optional<UserPortfolioItem> userPortfolioItem = this.getParticipants().stream()
                .map(userPortfolioImgMap::get)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(UserPortfolioItem::getSequence));
        userPortfolioItem.ifPresent(userPortfolioImg -> this.chatRoomThumbnail = userPortfolioImg.getItemUrl());
        userPortfolioItem.ifPresent(userPortfolioImg -> this.title = userPortfolioImg.getUserName());
    }

    @JsonIgnore
    public void setThumbnailAndRoomTitle(UserPortfolioItem userPortfolioItem) {
        Objects.requireNonNull(userPortfolioItem, "userPortfolioItem이 필요합니다.");
        this.chatRoomThumbnail = userPortfolioItem.getItemUrl();
        this.title = userPortfolioItem.getUserName();
    }

    @JsonIgnore
    public static List<Long> getAllParticipantIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .flatMap(chatRoom -> Objects.requireNonNull(chatRoom.getParticipants()).stream())
                .distinct()
                .toList();
    }

    @JsonIgnore
    public List<Long> getParticipantsIds() {
        return new ArrayList<>(Objects.requireNonNull(this.participants));
    }
}