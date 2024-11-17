package org.kiru.user.user.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.kiru.core.user.user.domain.User;
import org.kiru.core.user.userPortfolioImg.domain.UserPortfolio;
@Getter
@AllArgsConstructor
@Builder
public class GetMyInfoResponse {
        @NotNull
        private final Long id;

        @NotNull
        private final String username;

        @NotNull
        @Email
        private String email;
        @NotNull
        private String description;
        @NotNull
        private String instagramId;
        @NotNull
        private String webUrl;

        private UserPortfolio userPortfolio;

        private List<Integer> userPurposes;

        private List<String> userTalents;

        public static GetMyInfoResponse of(User user) {
            return GetMyInfoResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .description(user.getDescription())
                    .instagramId(user.getInstagramId())
                    .webUrl(user.getWebUrl())
                    .userTalents(user.getUserTalents().stream().map(i-> i.getTalentType().getDisplayName()).toList())
                    .userPurposes(user.getUserPurposes())
                    .userPortfolio(user.getUserPortfolio())
                    .build();
        }
    }
