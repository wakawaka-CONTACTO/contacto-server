package org.kiru.core.user.userPortfolioImg.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(name = "user_portfolio_imgs",
        indexes = @Index(name = "user_portfolio_imgs_user_id_idx", columnList = "user_id"))
@Getter
public class UserPortfolioImg {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @NotNull
    @Getter
    @Column(name = "portfolio_image_url")
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

    public void portfolioImageUrl(String portfolioImageUrl) {
        this.portfolioImageUrl = portfolioImageUrl;
    }
}
