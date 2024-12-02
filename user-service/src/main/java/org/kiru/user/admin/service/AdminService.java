package org.kiru.user.admin.service;


import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.user.admin.dto.AdminMatchedUserResponse;
import org.kiru.user.admin.dto.AdminUserDto;
import org.kiru.user.admin.dto.AdminUserDto.UserDto;
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

    public List<AdminMatchedUserResponse> getUsersByIds(List<Long> userIds) {
        return  userRepository.findSimpleUserByIds(userIds).stream()
                .map(user -> AdminMatchedUserResponse.of(user))
                .collect(Collectors.toList());
    }

    public List<AdminMatchedUserResponse> getAlreadyLikedUserIds(Long userId) {
        return chatApiClient.getMactedUsers(userId);
    }
}
