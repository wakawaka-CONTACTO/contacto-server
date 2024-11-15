package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.userPurpose.entity.UserPurpose;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioImgRepository;
import org.kiru.user.portfolio.service.out.GetUserPortfoliosQuery;
import org.kiru.user.user.repository.UserPurposeRepository;
import org.kiru.user.userlike.repository.UserLikeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
    private final UserPortfolioImgRepository userPortfolioImgRepository;
    private final UserPurposeRepository userPurposeRepository;
    private final GetUserPortfoliosQuery getUserPortfoliosQuery;
    private final UserLikeRepository userLikeRepository;

    @Transactional(readOnly = true)
    public List<UserPortfolioResDto> getUserPortfolios(Long userId) {
        List<Long> distinctUserIds = getDistinctUserIds(userId);
        log.error(distinctUserIds.toString());
        List<UserPortfolioResDto> portfolios = getUserPortfoliosQuery.findAllPortfoliosByUserIds(distinctUserIds);
        // Group by userId, username, and portfolioId
        Map<Long, Map<Long, List<UserPortfolioResDto>>> groupedPortfolios = portfolios.stream()
                .collect(Collectors.groupingBy(UserPortfolioResDto::getUserId,
                        Collectors.groupingBy(UserPortfolioResDto::getPortfolioId)));

        // Flatten the grouped results into a list of UserPortfolioResDto
        List<UserPortfolioResDto> result = new ArrayList<>();
        groupedPortfolios.forEach((userIdKey, portfolioMap) -> portfolioMap.forEach((portfolioIdKey, portfolioList) -> {
            String username = portfolioList.getFirst().getUsername();
            List<String> images = portfolioList.stream()
                    .flatMap(p -> p.getPortfolioImages().stream())
                    .toList();
            result.add(new UserPortfolioResDto(userIdKey, username, portfolioIdKey, images));
        }));
        return result;
    }

    private List<Long> getDistinctUserIds(Long userId) {
        List<Long> matchingUserIds = userPurposeRepository.findUserIdsByPurposeTypes(
                userPurposeRepository.findAllByUserId(userId).stream()
                        .map(UserPurpose::getPurposeType).toList());
        List<Long> likedUserIds = userLikeRepository.findAllByLikedUserId(userId);
        List<Long> popularIds = userLikeRepository.findAllUserIdOrderByLikedUserIdCountDesc();
        matchingUserIds.addAll(likedUserIds);
        matchingUserIds.addAll(popularIds);
        log.error(matchingUserIds.toString());
        return matchingUserIds.stream().distinct().toList();
    }
}