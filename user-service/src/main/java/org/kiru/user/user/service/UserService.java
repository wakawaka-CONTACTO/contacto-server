package org.kiru.user.user.service;


import jakarta.mail.Multipart;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.api.ChatApiClient;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final ChatApiClient chatApiClient;
    private final ImageService imageService;

    public User getUserFromIdToMainPage(Long userId) {
        User user = User.of(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND))
        );
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
        List<PurposeType> userPurposes = userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
        List<UserTalent> userTalents = userTalentRepository.findAllByUserId(userId);
        List<UserPortfolioImg> userPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        Long portfolioId = userPortfolioImgs.getFirst().getId();
        userPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        UserPortfolio userPortfolio = UserPortfolio.builder().portfolioId(portfolioId)
                .portfolioImages(userPortfolioImgs.stream().map(UserPortfolioImg::getPortfolioImageUrl).toList())
                .userId(userId).build();
        user.userPurposes(userPurposes);
        user.userTalents(userTalents);
        user.userPortfolio(userPortfolio);
        return user;
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateDto userUpdateDto) {
        UserJpaEntity existingUser = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND)
        );
        existingUser.updateDetails(
                User.builder().username(userUpdateDto.getUsername()).password(userUpdateDto.getPassword())
                        .description(userUpdateDto.getDescription())
                        .email(userUpdateDto.getEmail()).instagramId(userUpdateDto.getInstagramId())
                        .webUrl(userUpdateDto.getWebUrl()).build());
        userPurposeRepository.deleteAllByUserId(userId);
        List<UserPurpose> updatedPurposes = userUpdateDto.getUserPurposes().stream()
                .map(purposeType -> UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(purposeType)).build())
                .toList();
        userPurposeRepository.saveAll(updatedPurposes);
        userTalentRepository.deleteAllByUserId(userId);
        List<UserTalent> updatedTalents = userUpdateDto.getUserTalents().stream()
                .map(talent -> UserTalent.builder().userId(userId).talentType(talent).build())
                .toList();
        userTalentRepository.saveAll(updatedTalents);
        Map<Integer, MultipartFile> changedPortfolioImages = userUpdateDto.getPortfolioImages();
        List<UserPortfolioImg> existingPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        if (changedPortfolioImages != null) {
            existingPortfolioImgs.stream()
                    .map(img -> {
                        MultipartFile updatedImg = changedPortfolioImages.get(img.getSequence());
                        if (updatedImg != null) {
                            return imageService.updateImage(updatedImg, userId, img);
                        } else {
                            return img;
                        }
                    });
            userPortfolioRepository.saveAll(existingPortfolioImgs);
        }
        Long portfolioId = existingPortfolioImgs.isEmpty() ? null : existingPortfolioImgs.getFirst().getId();
        if (!existingPortfolioImgs.isEmpty()) {
            existingPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        }
        UserPortfolio userPortfolio = UserPortfolio.builder().portfolioId(portfolioId)
                .portfolioImages(existingPortfolioImgs.stream().map(UserPortfolioImg::getPortfolioImageUrl).toList())
                .userId(userId).build();
        log.error("userPortfolio: {}", userPortfolio);
        User updateUser = User.of(userRepository.save(existingUser));
        log.error("userPortfolio2: {}", userPortfolio);
        updateUser.userPurposes(updatedPurposes.stream().map(UserPurpose::getPurposeType).toList());
        updateUser.userTalents(updatedTalents);
        updateUser.userPortfolio(userPortfolio);
        log.error("userPortfolio2: {}", userPortfolio);
        return updateUser;
    }
}
