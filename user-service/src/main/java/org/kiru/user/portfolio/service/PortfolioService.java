package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioImgRepository;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.user.repository.UserRepository;
import org.kiru.user.userlike.repository.UserLikeRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final UserPortfolioImgRepository userPortfolioImgRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final UserRepository userRepository;
    private final UserLikeRepository userLikeRepository;

    public List<UserPortfolioResDto> getUserPortfolios(Long userId) {
        // userId의 purpose 찾기
        List<PurposeType> userPurposes = userPurposeRepository.findAllByUserId(userId).stream()
                .map(UserPurpose::getPurposeType).toList();
        // purpose가 일치하는 userIds 찾기
        List<Long> matchingUserIds = userPurposeRepository.findUserIdsByPurposeTypes(userPurposes);
        // 나를 좋아하는 사람들 찾기
        List<Long> likedUserIds = userLikeRepository.findAllByLikedUserId(userId);
        // 인기있는 사람들 찾기
        List<Long> popularIds = userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc();

        matchingUserIds.addAll(likedUserIds);
        matchingUserIds.addAll(popularIds);

        List<Long> distinctUserIds = matchingUserIds.stream().distinct().toList();
        // 포트폴리오 생성
        Map<Long, List<UserPortfolioImg>> portfolioImgMap = userPortfolioImgRepository.findAllByUserIdIn(distinctUserIds)
                .stream()
                .collect(Collectors.groupingBy(UserPortfolioImg::getUserId));
        List<UserPortfolioResDto> userPortfolios = new ArrayList<>();
        for (Map.Entry<Long, List<UserPortfolioImg>> entry : portfolioImgMap.entrySet()) {
            Long userIdByPortfolio = entry.getKey();
            List<UserPortfolioImg> portfolioImgs = entry.getValue();
            String username = userRepository.findById(userIdByPortfolio)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + userIdByPortfolio)).getUsername();
            UserPortfolioResDto userPortfolio = UserPortfolioResDto.builder()
                    .portfolioId(portfolioImgs.getFirst().getPortfolioId())
                    .userId(userIdByPortfolio)
                    .username(username)
                    .portfolioImages(portfolioImgs.stream()
                            .sorted(Comparator.comparingInt(UserPortfolioImg::getSequence))
                            .map(UserPortfolioImg::getPortfolioImageUrl)
                            .toList())
                    .build();
            userPortfolios.add(userPortfolio);
        }
        return userPortfolios;
    }
}