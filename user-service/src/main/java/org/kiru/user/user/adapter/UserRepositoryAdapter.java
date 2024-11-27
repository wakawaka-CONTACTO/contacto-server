package org.kiru.user.user.adapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.exception.EntityNotFoundException;
import org.kiru.user.exception.code.FailureCode;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.repository.UserPortfolioRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdateUseCase;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final PasswordEncoder passwordEncoder;

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
                .map(purposeType -> UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(purposeType))
                        .build())
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
    public List<UserPortfolioImg> updateUserPortfolioImages(final Long userId, UserUpdateDto userUpdateDto) {
        Map<Integer, Object> changedPortfolioImages = userUpdateDto.getPortfolioImages();
        List<UserPortfolioImg> existingPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        Long portfolioId = existingPortfolioImgs.getFirst().getPortfolioId();
        // 기존의 existingPortfolioImgs 삭제
        userPortfolioRepository.deleteAll(existingPortfolioImgs);
        // MultipartFile과 String 분리
        Map<Integer, MultipartFile> multipartImages = new HashMap<>();
        Map<Integer, String> stringImages = new HashMap<>();
        if (changedPortfolioImages != null) {
            for (Map.Entry<Integer, Object> entry : changedPortfolioImages.entrySet()) {
                Integer sequence = entry.getKey();
                Object updatedImg = entry.getValue();
                if (updatedImg instanceof MultipartFile) {
                    multipartImages.put(sequence, (MultipartFile) updatedImg);
                } else if (updatedImg instanceof String) {
                    stringImages.put(sequence, (String) updatedImg);
                }
            }
        }

        // MultipartFile 저장
        List<UserPortfolioImg> updatedPortfolioImgs = new ArrayList<>();
        if (!multipartImages.isEmpty()) {
            updatedPortfolioImgs.addAll(imageService.saveImagesWithSequence(multipartImages, userId, portfolioId));
        }

        // String 저장
        for (Map.Entry<Integer, String> entry : stringImages.entrySet()) {
            Integer sequence = entry.getKey();
            String imageUrl = entry.getValue();
            UserPortfolioImg newImg = UserPortfolioImg.of(userId, portfolioId, imageUrl, sequence);
            updatedPortfolioImgs.add(newImg);
        }
        updatedPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        return userPortfolioRepository.saveAll(updatedPortfolioImgs);
    }

    @Override
    public User updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto) {
        existingUser.setPassword(
                passwordEncoder.encode(userUpdatePwdDto.password()));
        return User.of(existingUser);
    }
}
