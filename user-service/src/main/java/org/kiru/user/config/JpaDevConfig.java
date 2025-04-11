package org.kiru.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@Profile("dev")
@EntityScan(basePackages = {"org.kiru.core.user", "org.kiru.core.user.like"})
@EnableJpaAuditing
public class JpaDevConfig {
}
