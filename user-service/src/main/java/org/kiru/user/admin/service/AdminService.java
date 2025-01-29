package org.kiru.user.admin.service;


import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.chat.message.domain.Message;
import org.kiru.user.admin.dto.AdminLikeUserResponse;
import org.kiru.user.admin.dto.AdminLikeUserResponse.AdminLikeUserDto;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.admin.service.out.AdminUserQuery;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.dto.UserIdUsername;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminUserQuery adminUserQuery;
    private final ChatApiClient chatApiClient;

    public List<AdminUserDto> getUsers(Pageable pageable) {
        List<UserDto> users = adminUserQuery.findAll(pageable);
        List<Long> connectedUserIds = chatApiClient.getConnectedUserIds();
        return users.stream()
                .map(user -> AdminUserDto.of(user, connectedUserIds.contains(user.id())))
                .toList();
    }

    public List<AdminUserDto> findUserByName(String name) {
        List<UserDto> user = adminUserQuery.findUserByName(name);
        List<Long> connectedUserIds = chatApiClient.getConnectedUserIds();
        return user.stream().map(u -> AdminUserDto.of(u, connectedUserIds.contains(u.id()))).toList();
    }

    public List<AdminMatchedUserResponse> getMatchedUsers(Long userId) {
        List<MatchedUserResponse> chatApiClientMatchedUsers = chatApiClient.getMatchedUsers(userId);
        List<Long> userIds = chatApiClientMatchedUsers.stream()
                .map(MatchedUserResponse::userId)
                .toList();
        List<UserIdUsername> userNames = adminUserQuery.findUsernamesByIds(userIds);
        List<AdminMatchedUserResponse> adminMatchedUserResponses =  chatApiClientMatchedUsers.stream()
                .map(matchedUser -> {
                    String name = userNames.stream()
                            .filter(user -> user.getId().equals(matchedUser.userId()))
                            .map(UserIdUsername::getUsername)
                            .findFirst()
                            .orElse(null);
                    return new AdminMatchedUserResponse(matchedUser.userId(), name, matchedUser.matchedAt());
                })
                .toList();
        return adminMatchedUserResponses.stream()
                .collect(Collectors.toMap(AdminMatchedUserResponse::userId, response -> response, (existing, replacement) -> existing))
                .values().stream()
                .sorted(Comparator.comparing(AdminMatchedUserResponse::matchedAt).reversed())
                .toList();
    }

    public ChatRoom getRoom(Long roomId, Long userId) {
        return chatApiClient.adminGetChatRoom(roomId, userId, true);
    }
    public ChatRoom getOrCreateCsChatRoom(Long adminId, Long userId) {
        return chatApiClient.getOrCreateCsChatRoom(adminId,userId);
    }

    private List<AdminLikeUserDto> getUserLikesInternal(Pageable pageable, Long userId, String name, boolean isLiked) {
        List<AdminLikeUserDto> result;
        if (isLiked) {
            if (name == null) {
                result = adminUserQuery.findUserLiked(pageable, userId);
            } else {
                result = adminUserQuery.findUserLikedByName(pageable, userId, name);
            }
        } else {
            result = (name == null ? adminUserQuery.findUserLikes(pageable, userId) : adminUserQuery.findUserLikesByName(pageable, userId, name));
        }
        return result;
    }

    public AdminLikeUserResponse getUserLikesAndUserLiked(Pageable pageable, Long userId) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<AdminLikeUserDto>> userLikesFuture = CompletableFuture.supplyAsync(() -> getUserLikesInternal(pageable, userId, null, false), executor);
            CompletableFuture<List<AdminLikeUserDto>> userLikedFuture = CompletableFuture.supplyAsync(() -> getUserLikesInternal(pageable, userId, null, true), executor);
            CompletableFuture<Void> allOf = CompletableFuture.allOf(userLikesFuture, userLikedFuture);
            return AdminLikeUserResponse.of(userLikesFuture.join(), userLikedFuture.join());
        }
    }

    public AdminLikeUserResponse getUserLikesAndUserLikedByName(Pageable pageable, Long userId, String name) {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CompletableFuture<List<AdminLikeUserDto>> userLikesFuture = CompletableFuture.supplyAsync(() -> getUserLikesInternal(pageable, userId, name, false), executor);
            CompletableFuture<List<AdminLikeUserDto>> userLikedFuture = CompletableFuture.supplyAsync(() -> getUserLikesInternal(pageable, userId, name, true), executor);
            CompletableFuture<Void> allOf = CompletableFuture.allOf(userLikesFuture, userLikedFuture);
            return AdminLikeUserResponse.of(userLikesFuture.join(), userLikedFuture.join());
        }
    }

    public Slice<Message> getMessages(Long roomId, Long userId, Pageable pageable) {
        return chatApiClient.getMessages(roomId, userId, pageable);
    }
}
