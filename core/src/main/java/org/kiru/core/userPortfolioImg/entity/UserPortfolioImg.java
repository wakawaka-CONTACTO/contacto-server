package org.kiru.core.userPortfolioImg.entity;

import jakarta.persistence.Entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_portfolio_imgs")
@Getter
public class UserPortfolioImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long userId;

    @NotNull
    private Long portfolioId;

    @NotNull
    @Getter
    private String portfolioImageUrl;

    @NotNull
    @Getter
    private int sequence;

    public static UserPortfolioImg of(Long userId, Long portfolioId, String portfolioImageUrl, int sequence) {
        return UserPortfolioImg.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .portfolioImageUrl(portfolioImageUrl)
                .sequence(sequence)
                .build();
    }
}
