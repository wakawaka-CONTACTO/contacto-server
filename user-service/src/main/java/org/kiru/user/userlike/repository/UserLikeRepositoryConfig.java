package org.kiru.user.userlike.repository;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "org.kiru.user.userlike.repository")
public class UserLikeRepositoryConfig {
}
