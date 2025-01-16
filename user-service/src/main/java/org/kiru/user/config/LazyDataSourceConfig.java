package org.kiru.user.config;

import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Profile({"dev","test"})
public class LazyDataSourceConfig {
    @Value("${db.connections}")
    private int connections;

    @Bean
    public DataSource lazyDataSource(DataSourceProperties properties) {
        HikariDataSource dataSource = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .driverClassName(properties.determineDriverClassName())
                .password(properties.determinePassword())
                .url(properties.determineUrl())
                .username(properties.determineUsername())
                .build();
        dataSource.setMaximumPoolSize(connections); // Connection pool size 설정
        dataSource.setPoolName("HikariPool-User");
        return new LazyConnectionDataSourceProxy(dataSource);
    }
}
