package org.kiru.gateway.config;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import java.util.Arrays;
import java.util.List;
import org.kiru.core.user.user.entity.NationalityConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "org.kiru.gateway.common")
public class R2dbcConfig extends AbstractR2dbcConfiguration {
  @Value("${spring.r2dbc.url}")
  private String r2dbcUrl;

  @Override
  public ConnectionFactory connectionFactory() {
    return ConnectionFactories.get(r2dbcUrl);
  }

  @Override
  protected List<Object> getCustomConverters() {
    return Arrays.asList(
        new NationalityConverter()
    );
  }
}
