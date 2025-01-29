package org.kiru.core.user.userPortfolioItem.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolioItem;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Table(name = "user_portfolio_imgs",
        indexes = @Index(name = "user_portfolio_imgs_user_id_idx", columnList = "user_id"))
@Getter
public class UserPortfolioImg implements UserPortfolioItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "userId가 없습니다.")
    @Column(name = "user_id")
    private Long userId;

    @NotNull(message = "portfolio_id가 없습니다.")
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @NotNull(message = "portfolio_image_url이 없습니다.")
    @Column(name = "portfolio_image_url")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY,value = "itemUrl") // JSON에서 읽기 전용으로 설정
    private String portfolioImageUrl;

    @NotNull(message = "sequence가 없습니다.")
    @Getter
    private int sequence;

    @Getter
    @Column(name = "username")
    private String userName;

    public static UserPortfolioImg of(Long userId, Long portfolioId, String portfolioImageUrl, int sequence, String userName) {
        return UserPortfolioImg.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .portfolioImageUrl(portfolioImageUrl)
                .sequence(sequence)
                .userName(userName)
                .build();
    }

    public static UserPortfolioImg toEntity(UserPortfolioItem userPortfolioItem) {
        return UserPortfolioImg.builder()
                .userId(userPortfolioItem.getUserId())
                .portfolioId(userPortfolioItem.getPortfolioId())
                .portfolioImageUrl(userPortfolioItem.getItemUrl())
                .sequence(userPortfolioItem.getSequence())
                .userName(userPortfolioItem.getUserName())
                .build();
    }

    public static UserPortfolioItem toModel(UserPortfolioImg entity) {
        return UserPortfolioImg.builder()
                .userId(entity.getUserId())
                .portfolioId(entity.getPortfolioId())
                .portfolioImageUrl(entity.getPortfolioImageUrl())
                .sequence(entity.getSequence())
                .userName(entity.getUserName())
                .build();
    }

    public void portfolioImageUrl(String portfolioImageUrl) {
        this.portfolioImageUrl = portfolioImageUrl;
    }

    @Override
    public String getItemUrl() {
        return this.portfolioImageUrl;
    }
}
