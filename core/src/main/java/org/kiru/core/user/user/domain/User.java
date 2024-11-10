package org.kiru.core.user.user.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.user.talent.entity.UserTalent;
import org.kiru.core.user.user.entity.UserJpaEntity;
import org.kiru.core.user.userPortfolioImg.domain.UserPortfolio;
import org.kiru.core.user.userPurpose.domain.PurposeType;

@Getter
@AllArgsConstructor
@Builder
public class User {
    @NotNull
    private final Long id;
    @NotNull
    private final String username;

    private final String socialId;
    @NotNull
    private final LoginType loginType;
    @NotNull
    @Email
    private String email;
    @NotNull
    private String description;
    @NotNull
    private String instagramId;
    @NotNull
    private String webUrl;

    private String password;

    private UserPortfolio userPortfolio;

    private List<PurposeType> userPurposes;

    private List<UserTalent> userTalents;

    public void userPortfolio(UserPortfolio userPortfolio){
        this.userPortfolio = userPortfolio;
    }
    public void userPurposes(List<PurposeType> userPurposes){
        this.userPurposes = userPurposes;
    }
    public void userTalents(List<UserTalent> userTalents){
        this.userTalents = userTalents;
    }

    public static User of(UserJpaEntity user) {
        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .loginType(user.getLoginType())
                .socialId(user.getSocialId())
                .email(user.getEmail())
                .description(user.getDescription())
                .instagramId(user.getInstagramId())
                .webUrl(user.getWebUrl())
                .build();
    }
}
