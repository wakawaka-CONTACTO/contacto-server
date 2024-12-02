package org.kiru.user.user.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.chat.chatroom.domain.ChatRoom;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.exception.EntityNotFoundException;
import org.kiru.user.exception.code.FailureCode;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.dto.response.UpdatePwdResponse;
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.in.GetUserMainPageUseCase;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService implements GetUserMainPageUseCase {
    private final UserQueryWithCache userQueryWithCache;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserRepository userRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final UserUpdateUseCase userUpdateUseCase;
    private final ChatApiClient chatApiClient;

    public User getUserFromIdToMainPage(Long userId) {
        User user = User.of(userQueryWithCache.getUser(userId));
        return getUserDetails(user);
    }

    public List<ChatRoom> getUserChatRooms(Long userId) {
        List<ChatRoom> chatRooms = chatApiClient.getUserChatRooms(userId);
        List<Long> allParticipantIds = getAllParticipantIds(chatRooms);

        Map<Long, UserPortfolioImg> userPortfolioImgMap = getUserPortfolioImgMap(allParticipantIds);
        Map<Long, String> userIdToUsernameMap = getUserIdToUsernameMap(allParticipantIds);

        for (ChatRoom chatRoom : chatRooms) {
            setChatRoomThumbnail(chatRoom, userPortfolioImgMap);
            setChatRoomTitle(chatRoom, userId, userIdToUsernameMap);
        }
        return chatRooms;
    }

public ChatRoom getUserChatRoom(Long roomId, Long userId) {
    ChatRoom chatRoom = chatApiClient.getRoom(roomId, userId);
    List<Long> participantIds = chatRoom.getParticipants().stream()
            .filter(participantId -> !participantId.equals(userId))
            .toList();
    Map<Long, UserPortfolioImg> userPortfolioImgMap = getUserPortfolioImgMap(participantIds);
    Map<Long, String> userIdToUsernameMap = getUserIdToUsernameMap(participantIds);
    setChatRoomThumbnail(chatRoom, userPortfolioImgMap);
    setChatRoomTitle(chatRoom, userId, userIdToUsernameMap);
    return chatRoom;
}

    private User getUserDetails(User user) {
        Long userId = user.getId();
        user.userPurposes(getUserPurposes(userId));
        user.userTalents(userTalentRepository.findAllByUserId(userId));
        user.userPortfolio(getUserPortfolio(userId));
        return user;
    }

    private List<PurposeType> getUserPurposes(Long userId) {
        return userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
    }

    private UserPortfolio getUserPortfolio(Long userId) {
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        if (userPortfolioImgs.isEmpty()) {
            return null;
        }
        userPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        Long portfolioId = userPortfolioImgs.get(0).getId();
        return UserPortfolio.builder()
                .portfolioId(portfolioId)
                .portfolioImages(userPortfolioImgs.stream().map(UserPortfolioImg::getPortfolioImageUrl).toList())
                .userId(userId)
                .build();
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        UserJpaEntity existingUser = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND)
        );
        updateUserDetails(existingUser, userUpdateDto);
        CompletableFuture<List<UserPurpose>> purposesFuture = CompletableFuture.supplyAsync(
                () -> userUpdateUseCase.updateUserPurposes(userId, userUpdateDto));


        CompletableFuture<List<UserTalent>> talentsFuture = CompletableFuture.supplyAsync(
                () -> userUpdateUseCase.updateUserTalents(userId, userUpdateDto));
        CompletableFuture<List<UserPortfolioImg>> portfolioImgsFuture = CompletableFuture.supplyAsync(
                () -> userUpdateUseCase.updateUserPortfolioImages(userId, userUpdateDto));
        CompletableFuture.allOf(purposesFuture, talentsFuture, portfolioImgsFuture).join();
        User updatedUser = User.of(userQueryWithCache.saveUser(existingUser));
        updatedUser.userPurposes(purposesFuture.join().stream().map(UserPurpose::getPurposeType).toList());
        updatedUser.userTalents(talentsFuture.join());
        updatedUser.userPortfolio(getUserPortfolio(userId));
        return updatedUser;
    }

    public void updateUserDetails(UserJpaEntity existingUser, UserUpdateDto userUpdateDto) {
        existingUser.updateDetails(
                User.builder()
                        .username(userUpdateDto.getUsername())
                        .password(userUpdateDto.getPassword())
                        .description(userUpdateDto.getDescription())
                        .email(userUpdateDto.getEmail())
                        .instagramId(userUpdateDto.getInstagramId())
                        .webUrl(userUpdateDto.getWebUrl())
                        .build()
        );
    }

    private List<Long> getAllParticipantIds(List<ChatRoom> chatRooms) {
        return chatRooms.stream()
                .flatMap(chatRoom -> chatRoom.getParticipants().stream())
                .distinct()
                .toList();
    }

    private Map<Long, UserPortfolioImg> getUserPortfolioImgMap(List<Long> allParticipantIds) {
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserIdInWithMinSequence(
                allParticipantIds);
        return userPortfolioImgs.stream()
                .collect(Collectors.toMap(UserPortfolioImg::getUserId, img -> img));
    }

    private Map<Long, String> getUserIdToUsernameMap(List<Long> allParticipantIds) {
        List<Object[]> userIdAndUsernames = userRepository.findUsernamesByIds(allParticipantIds);
        return userIdAndUsernames.stream()
                .collect(Collectors.toMap(
                        userIdAndUsername -> Long.parseLong(userIdAndUsername[0].toString()),
                        userIdAndUsername -> (String) userIdAndUsername[1]
                ));
    }

    private void setChatRoomThumbnail(ChatRoom chatRoom, Map<Long, UserPortfolioImg> userPortfolioImgMap) {
        Optional<UserPortfolioImg> thumbnail = chatRoom.getParticipants().stream()
                .map(userPortfolioImgMap::get)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(UserPortfolioImg::getSequence));
        thumbnail.ifPresent(userPortfolioImg -> chatRoom.setChatRoomThumbnail(userPortfolioImg.getPortfolioImageUrl()));
    }

    private void setChatRoomTitle(ChatRoom chatRoom, Long userId, Map<Long, String> userIdToUsernameMap) {
        assert chatRoom.getParticipants() != null;
        List<String> otherUsernames = chatRoom.getParticipants().stream()
                .filter(participantId -> !participantId.equals(userId))
                .map(userIdToUsernameMap::get)
                .filter(Objects::nonNull)
                .toList();
        if (!otherUsernames.isEmpty()) {
            chatRoom.setTitle(otherUsernames.getFirst());
        }
    }

    @Transactional
    public Boolean updateUserPwd(UserUpdatePwdDto userUpdatePwdDto) {
        Optional<UserJpaEntity> existingUser = userRepository.findByEmail(userUpdatePwdDto.email());
        if (existingUser.isPresent()) {
            return userUpdateUseCase.updateUserPwd(existingUser.get(), userUpdatePwdDto);
        } else {
            return false;
        }
    }

    public void findExistUserByEmail(String email) {
        userRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND)
        );
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }
}