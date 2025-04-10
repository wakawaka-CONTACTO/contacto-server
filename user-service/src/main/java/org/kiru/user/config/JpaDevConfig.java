package org.kiru.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@Profile("dev")
@EnableJpaAuditing
public class JpaDevConfig {
}
