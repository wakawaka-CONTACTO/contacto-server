package org.kiru.user.portfolio.controller;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.dto.res.UserScrapeImagesResDto;
import org.kiru.user.portfolio.service.PortfolioService;
import org.kiru.user.portfolio.service.ScraperService;
import org.kiru.user.user.dto.response.UserWithAdditionalInfoResponse;
import org.kiru.user.user.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/users/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;
    private final ScraperService scraperService;

    @GetMapping
    public ResponseEntity<List<UserPortfolioResDto>> getPortfolios(@UserId Long userId, Pageable pageable) {
        List<UserPortfolioResDto> portfolios = portfolioService.getUserPortfolios(userId, pageable);
        log.info("추천한 유저들의 포트폴리오 개수: {}", portfolios.size());
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{portfolioUserId}")
    public ResponseEntity<UserWithAdditionalInfoResponse> getOtherPortfolio(@PathVariable Long portfolioUserId) {
        User user = userService.getUserFromIdToMainPage(portfolioUserId);
        return ResponseEntity.ok(UserWithAdditionalInfoResponse.of(user));
    }

    @GetMapping("/instagram")
    public ResponseEntity<UserScrapeImagesResDto> getImages(@RequestParam("username") String username) {
        return ResponseEntity.ok(UserScrapeImagesResDto.from(scraperService.fetchImageUrls(username)));
    }
}
