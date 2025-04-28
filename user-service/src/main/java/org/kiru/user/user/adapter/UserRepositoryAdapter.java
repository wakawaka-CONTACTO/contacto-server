package org.kiru.user.user.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.exception.EntityNotFoundException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;
import org.kiru.core.user.userPortfolioItem.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.external.s3.ImageService;
import org.kiru.user.external.s3.S3Service;
import org.kiru.user.portfolio.repository.UserPortfolioRepository;
import org.kiru.user.user.dto.request.UserUpdateDto;
import org.kiru.user.user.dto.request.UserUpdatePwdDto;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.user.repository.UserTalentRepository;
import org.kiru.user.user.service.out.GetUserAdditionalInfoQuery;
import org.kiru.user.user.service.out.UserQueryWithCache;
import org.kiru.user.user.service.out.UserUpdatePort;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
public class UserRepositoryAdapter implements UserQueryWithCache, UserUpdatePort, GetUserAdditionalInfoQuery {
    private final UserRepository userRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final UserTalentRepository userTalentRepository;
    private final UserPortfolioRepository userPortfolioRepository;
    private final ImageService imageService;
    private final PasswordEncoder passwordEncoder;
    private final S3Service s3Service;

    @Override
    @Cacheable(value = "user", key = "#userId", unless = "#result == null")
    public User getUser(Long userId) {
        return UserJpaEntity.toModel(userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND)));
    }

    @Override
    @CachePut(value = "user", key = "#user.id", unless = "#result == null")
    public User saveExistUser(User user) {
      if(!userRepository.existsById(user.getId())){
             throw new EntityNotFoundException(FailureCode.ENTITY_NOT_FOUND);
      }
        return UserJpaEntity.toModel(userRepository.save(UserJpaEntity.of(user)));
    }

    @Transactional
    public List<PurposeType> updateUserPurposes(Long userId, UserUpdateDto userUpdateDto) {
        userPurposeRepository.deleteAllByUserId(userId);
        return userPurposeRepository.saveAll(userUpdateDto.getUserPurposes().stream()
                .map(purposeType -> UserPurpose.builder().userId(userId).purposeType(PurposeType.fromIndex(purposeType))
                        .build())
                .toList()).stream().map(UserPurpose::getPurposeType).toList();
    }

    @Override
    @Transactional
    public List<TalentType> updateUserTalents(Long userId, UserUpdateDto userUpdateDto) {
        userTalentRepository.deleteAllByUserId(userId);
        return userTalentRepository.saveAll(userUpdateDto.getUserTalents().stream()
                .map(talent -> UserTalent.builder().userId(userId).talentType(talent).build())
                .toList()).stream().map(UserTalent::getTalentType).toList();
    }

    @Override
    @Transactional
    public UserPortfolio updateUserPortfolioImages(final Long userId, UserUpdateDto userUpdateDto) {
        Map<Integer, Object> changedPortfolioImages = userUpdateDto.getPortfolio();
        UserPortfolio userPortfolio = UserPortfolio.withUserId(userId);

        // 이전 포트폴리오 URL 수집
        Set<String> beforeUpdatePortfolio = userPortfolioRepository.findAllByUserId(userId).stream()
            .map(UserPortfolioImg::getPortfolioImageUrl)
            .collect(Collectors.toSet());

        List<UserPortfolioItem> existingImages = imageService.verifyExistingImages(changedPortfolioImages, userPortfolio,
            userUpdateDto.getUsername());

        // 유지되는 이미지 URL 제거
        existingImages.stream()
            .map(UserPortfolioItem::getItemUrl)
            .forEach(beforeUpdatePortfolio::remove);

        // 남은 URL은 S3에서 삭제
        beforeUpdatePortfolio.forEach(s3Service::deleteImage);

        List<UserPortfolioItem> newPortfolioItem = imageService.saveImagesS3WithSequence(changedPortfolioImages,
            userPortfolio, userUpdateDto.getUsername());

        List<UserPortfolioItem> updatePortfolioItems = new ArrayList<>();
        updatePortfolioItems.addAll(newPortfolioItem);
        updatePortfolioItems.addAll(existingImages);
        if (updatePortfolioItems.isEmpty()){
            throw new EntityNotFoundException(FailureCode.INVALID_PORTFOLIO_EDIT);
        }

        userPortfolioRepository.deleteAllByUserId(userId);

        List<UserPortfolioItem> savedItems = userPortfolioRepository.saveAll(
            updatePortfolioItems.stream()
                .map(UserPortfolioImg::toEntity)
                .toList()
        ).stream().map(UserPortfolioImg::toModel).toList();

        return UserPortfolio.builder().portfolioId(userPortfolio.getPortfolioId())
            .userId(userId)
            .portfolioItems(new ArrayList<>(savedItems)).build();
    }


    @Transactional
    public Boolean updateUserPwd(UserJpaEntity existingUser, UserUpdatePwdDto userUpdatePwdDto) {
        existingUser.setPassword(
                passwordEncoder.encode(userUpdatePwdDto.password()));
        return true;
    }

    @Override
    public List<PurposeType> getUserPurposes(Long userId) {
        return userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
    }

    @Override
    public List<TalentType> getUserTalents(Long userId) {
        return userTalentRepository.findAllByUserId(userId).stream()
                .map(UserTalent::getTalentType).toList();
    }

    @Override
    public List<UserPortfolioItem> getUserPortfolioByUserId(Long userId) {
        return userPortfolioRepository.findAllByUserId(userId).stream()
                .map(UserPortfolioImg::toModel)
                .toList();
    }
}
