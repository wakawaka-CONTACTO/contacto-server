package org.kiru.user.admin.service;


import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
import org.kiru.user.admin.dto.MatchedUserResponse;
import org.kiru.user.admin.service.out.AdminUserQuery;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminUserQuery adminUserQuery;
    private final ChatApiClient chatApiClient;
    private final UserRepository userRepository;

    public Page<AdminUserDto> getUsers(Pageable pageable) {
        Page<UserDto> users = adminUserQuery.findAll(pageable);
        List<Long> connectedUserIds = chatApiClient.getConnectedUserIds();
        return users.map(user -> AdminUserDto.of(user, connectedUserIds.contains(user.id())));
    }

    public AdminUserDto findUserByName(String name) {
        UserDto user = adminUserQuery.findUserByName(name);
        List<Long> connectedUserIds = chatApiClient.getConnectedUserIds();
        return AdminUserDto.of(user, connectedUserIds.contains(user.id()));
    }

    public List<AdminMatchedUserResponse> getMatchedUsers(Long userId) {
        List<MatchedUserResponse> chatApiClientMatchedUsers = chatApiClient.getMatchedUsers(userId);
        List<Long> userIds = chatApiClientMatchedUsers.stream()
                .map(MatchedUserResponse::userId)
                .collect(Collectors.toList());
        List<Object[]> userNames = userRepository.findUsernamesByIds(userIds);
        return chatApiClientMatchedUsers.stream()
                .map(matchedUser -> {
                    String name = userNames.stream()
                            .filter(user -> user[0].equals(matchedUser.userId()))
                            .map(user -> (String) user[1])
                            .findFirst()
                            .orElse(null);
                    return new AdminMatchedUserResponse(matchedUser.userId(), name, matchedUser.matchedAt());
                })
                .toList();
    }
}
