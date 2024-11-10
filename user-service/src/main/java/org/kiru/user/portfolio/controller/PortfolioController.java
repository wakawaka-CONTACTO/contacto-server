package org.kiru.user.portfolio.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.kiru.core.user.user.domain.User;
import org.kiru.user.auth.argumentresolve.UserId;
import org.kiru.user.portfolio.dto.res.UserPortfolioResDto;
import org.kiru.user.portfolio.service.PortfolioService;
import org.kiru.user.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserPortfolioResDto>> getPortfolios(@UserId Long userId) {
        List<UserPortfolioResDto> portfolios = portfolioService.getUserPortfolios(userId);
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{portfolioUserId}")
    public ResponseEntity<User> getOtherPortfolio(@PathVariable Long portfolioUserId) {
        User user = userService.getUserFromIdToMainPage(portfolioUserId);
        return ResponseEntity.ok(user);
    }
}
