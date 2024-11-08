package org.kiru.user.auth.kakao;


import lombok.Getter;

public abstract class KaKaoDto {
    @Getter
    public static class KakaoUserInfoResponse {

        private Long id;
        private String connected_at;
        private KakaoProperties properties;
        private KakaoAccount kakao_account;
    }

    @Getter
    public static class KakaoProperties {
        private String nickname;
        private String profile_image;
        private String thumbnail_image;
    }

    @Getter
    public static class KakaoAccount {
        private Boolean profile_nickname_needs_agreement;
        private Boolean profile_image_needs_agreement;
        private KakaoProfile profile;
    }

    @Getter
    public static class KakaoProfile {
        private String nickname;
        private String thumbnail_image_url;
        private String profile_image_url;
        private Boolean is_default_image;
    }

}
