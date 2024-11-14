package org.kiru.user.user.adapter;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.exception.EntityNotFoundException;
import org.kiru.user.exception.code.FailureCode;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Repository
@Transactional
public class UserRepositoryAdapter implements UserQueryWithCache, UserUpdateUseCase {
    private final UserRepository userRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final ImageService imageService;
    @Override
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public UserJpaEntity getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND));
    }

    @Override
    @CachePut(value = "user", key = "#user.id", unless = "#result == null")
    public UserJpaEntity saveUser(UserJpaEntity user) {
        return userRepository.save(user);
    }

    @Transactional
    public List<UserPurpose> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto) {
        userPurposeRepository.deleteAllByUserId(userId);
        List<UserPurpose> updatedPurposes = userUpdateDto.getUserPurposes().stream()
                .map(purposeType -> UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(purposeType)).build())
                .toList();
        return userPurposeRepository.saveAll(updatedPurposes);
    }

    @Override
    @Transactional
    public List<UserTalent> updateUserTalents(Long userId, UserUpdateDto userUpdateDto) {
        userTalentRepository.deleteAllByUserId(userId);
        List<UserTalent> updatedTalents = userUpdateDto.getUserTalents().stream()
                .map(talent -> UserTalent.builder().userId(userId).talentType(talent).build())
                .toList();
        return userTalentRepository.saveAll(updatedTalents);
    }

    @Override
    @Transactional
    public List<UserPortfolioImg> updateUserPortfolioImages(Long userId, UserUpdateDto userUpdateDto) {
        Map<Integer, MultipartFile> changedPortfolioImages = userUpdateDto.getPortfolioImages();
        List<UserPortfolioImg> existingPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        if (changedPortfolioImages != null) {
            existingPortfolioImgs.forEach(img -> {
                MultipartFile updatedImg = changedPortfolioImages.get(img.getSequence());
                if (updatedImg != null) {
                    imageService.updateImage(updatedImg, userId, img);
                }
            });
            return userPortfolioRepository.saveAll(existingPortfolioImgs);
        }
        return existingPortfolioImgs;
    }
}
