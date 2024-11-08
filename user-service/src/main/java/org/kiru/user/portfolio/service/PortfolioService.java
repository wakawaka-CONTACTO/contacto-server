package org.kiru.user.portfolio.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.domain.User;
import org.kiru.core.userPortfolioImg.entity.UserPortfolioImg;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.repository.UserPortfolioImgRepository;
import org.kiru.user.user.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final UserPortfolioImgRepository userPortfolioImgRepository;
    private final UserRepository userRepository;

    public List<UserPortfolioResDto> getUserPortfolios(Long userId) {
        Map<Long, List<UserPortfolioImg>> portfolioImgMap = userPortfolioImgRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(UserPortfolioImg::getUserId));
        List<UserPortfolioResDto> userPortfolios = new ArrayList<>();
        for (Map.Entry<Long, List<UserPortfolioImg>> entry : portfolioImgMap.entrySet()) {
            Long userIdByPortfolio = entry.getKey();
            if(userId!=userIdByPortfolio){
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
        }
        return userPortfolios;
    }
}

