package org.kiru.user.admin.service;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    public List<AdminUserDto> findUserByName(String name, Pageable pageable) {
        List<UserDto> user = adminUserQuery.findUserByName(name, pageable);
        List<Long> connectedUserIds = chatApiClient.getConnectedUserIds();
        return user.stream().map(u -> AdminUserDto.of(u, connectedUserIds.contains(u.id()))).toList();
    }

    public List<AdminMatchedUserResponse> getMatchedUsers(Long userId, Pageable pageable) {
        Map<Long,MatchedUserResponse> matchedUsers = adminUserQuery.findMatchedUsersWithMatchedTime(userId, pageable);
        List<Long> userIds = matchedUsers.keySet().stream().toList();
        List<UserIdUsername> userNames = adminUserQuery.findUsernamesByIds(userIds);
        List<AdminMatchedUserResponse> adminMatchedUserResponses =  userNames.stream()
                .map(matchedUser -> {
                    return new AdminMatchedUserResponse(matchedUser.getId(), matchedUser.getUsername(), matchedUsers.get(matchedUser.getId()).matchedAt());
                })
                .toList();
        return adminMatchedUserResponses.stream()
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
            CompletableFuture.allOf(userLikesFuture, userLikedFuture);
            return AdminLikeUserResponse.of(userLikesFuture.join(), userLikedFuture.join());
        }
    }

    public Slice<Message> getMessages(Long roomId, Long userId, Boolean isAdmin, Pageable pageable) {
        return chatApiClient.getMessages(roomId, userId, isAdmin, pageable);
    }
}
