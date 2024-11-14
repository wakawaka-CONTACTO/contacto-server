package org.kiru.user.user.service;
import static java.util.stream.Collectors.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.in.GetUserMainPageUseCase;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return chatApiClient.getUserChatRooms(userId);
    }

    public ChatRoom getUserChatRoom(Long roomId, Long userId) {
        return chatApiClient.getRoom(roomId, userId);
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
        CompletableFuture<List<UserPurpose>> purposesFuture = CompletableFuture.supplyAsync(() -> userUpdateUseCase.updateUserPurposes(userId, userUpdateDto));
        CompletableFuture<List<UserTalent>> talentsFuture = CompletableFuture.supplyAsync(() -> userUpdateUseCase.updateUserTalents(userId, userUpdateDto));
        CompletableFuture<List<UserPortfolioImg>> portfolioImgsFuture = CompletableFuture.supplyAsync(() -> userUpdateUseCase.updateUserPortfolioImages(userId, userUpdateDto));
        CompletableFuture.allOf(purposesFuture, talentsFuture, portfolioImgsFuture).join();
        User updatedUser = User.of(userQueryWithCache.saveUser(existingUser));
        updatedUser.userPurposes(purposesFuture.join().stream().map(UserPurpose::getPurposeType).toList());
        updatedUser.userTalents(talentsFuture.join());
        updatedUser.userPortfolio(getUserPortfolio(userId));
        return updatedUser;
    }

    @Transactional
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
}