package org.kiru.core.user.user.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kiru.core.user.user.domain.LoginType;
import org.kiru.core.user.user.domain.Nationality;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class UserR2dbcEntity {
    @Id
    private Long id;

    @Column("username")
    private String username;

    @Column("description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column("nationality")
    private Nationality nationality;

    @Column("login_type")
    private LoginType loginType;

    @Column("email")
    private String email;

    @Column("web_url")
    private String webUrl;

    @Column("instagram_id")
    private String instagramId;

    @Column("deleted")
    private Boolean deleted;
}
