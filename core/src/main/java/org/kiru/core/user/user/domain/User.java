package org.kiru.core.user.user.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.exception.ForbiddenException;
import org.kiru.core.exception.code.FailureCode;
import org.kiru.core.user.talent.domain.Talent.TalentType;
import org.kiru.core.user.user.entity.UserR2dbcEntity;
import org.kiru.core.user.userPortfolioItem.domain.UserPortfolio;
import org.kiru.core.user.userPurpose.domain.PurposeType;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class User {
    @NotNull
    private Long id;

    @NotNull
    private String username;

    private Nationality nationality;

    private LoginType loginType;

    @NotNull
    @Email
    private String email;

    @NotNull
    private String description;

    @NotNull
    private String instagramId;

    private String webUrl;

    private String password;

    private UserPortfolio userPortfolio;

    private List<Integer> userPurposes;

    private List<TalentType> userTalents;

    public static User of(UserR2dbcEntity user) {
        return User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .loginType(user.getLoginType())
                .nationality(user.getNationality())
                .email(user.getEmail())
                .description(user.getDescription())
                .instagramId(user.getInstagramId())
                .webUrl(user.getWebUrl())
                .build();
    }

    public void userPortfolio(UserPortfolio userPortfolio) {
        this.userPortfolio = userPortfolio;
    }

    public void userPurposes(List<PurposeType> userPurposes) {
        this.userPurposes = userPurposes.stream().map(PurposeType::getIndex).toList();
    }

    public void userTalents(List<TalentType> userTalents) {
        this.userTalents = userTalents;
    }

    public void password(String password) {
        if (password == null || password.isEmpty()) {
            throw new ForbiddenException(FailureCode.INVALID_PASSWORD);
        }
        this.password = password;
    }
}
