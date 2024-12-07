package org.kiru.user.user.adapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.dto.response.UpdatePwdResponse;
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
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Bool;

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
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public List<UserPurpose> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto) {
        userPurposeRepository.deleteAllByUserId(userId);
        return userUpdateDto.getUserPurposes().stream()
                .map(purposeType -> UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(purposeType))
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public List<UserTalent> updateUserTalents(Long userId, UserUpdateDto userUpdateDto) {
        userTalentRepository.deleteAllByUserId(userId);
        return userUpdateDto.getUserTalents().stream()
                .map(talent -> UserTalent.builder().userId(userId).talentType(talent).build())
                .toList();
    }

    @Override
    @Transactional
    public List<UserPortfolioImg> updateUserPortfolioImages(final Long userId, UserUpdateDto userUpdateDto) {
        Map<Integer, Object> changedPortfolioImages = userUpdateDto.getPortfolioImages();
        List<UserPortfolioImg> existingPortfolioImgs = userPortfolioRepository.findAllByUserId(userId);
        Long portfolioId = existingPortfolioImgs.getFirst().getPortfolioId();
        Map<Integer, MultipartFile> multipartImages = new HashMap<>();
        Map<Integer, String> stringImages = new HashMap<>();
        if (changedPortfolioImages != null) {
            for (Entry<Integer, Object> entry : changedPortfolioImages.entrySet()) {
                Integer sequence = entry.getKey();
                Object updatedImg = entry.getValue();
                switch (updatedImg) {
                    case MultipartFile multipartFile -> multipartImages.put(sequence, multipartFile);
                    case String imageUrl -> stringImages.put(sequence, imageUrl);
                    default -> throw new ContactoException(FailureCode.USER_UPDATE_FAILED);
                }
            }
        }
        List<UserPortfolioImg> updatedPortfolioImgs = new ArrayList<>();
        if (!multipartImages.isEmpty()) {
            updatedPortfolioImgs.addAll(imageService.saveImagesWithSequence(multipartImages, userId, portfolioId));
        }
        for (Entry<Integer, String> entry : stringImages.entrySet()) {
            Integer sequence = entry.getKey();
            String imageUrl = entry.getValue();
            UserPortfolioImg newImg = UserPortfolioImg.of(userId, portfolioId, imageUrl, sequence);
            updatedPortfolioImgs.add(newImg);
        }
        updatedPortfolioImgs.sort(Comparator.comparing(UserPortfolioImg::getSequence));
        userPortfolioRepository.deleteAllByUserId(userId);
        return updatedPortfolioImgs;
    }

    @Transactional
    public Boolean updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto) {
        existingUser.setPassword(
                passwordEncoder.encode(userUpdatePwdDto.password()));
        return true;
    }
}
