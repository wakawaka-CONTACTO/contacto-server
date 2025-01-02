package org.kiru.user.config;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Map;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

@Configuration
@Slf4j
@Profile({"docker", "local"})
@RequiredArgsConstructor
public class MultiDataSourceConfig {

    @Value("${db.connections}")
    private int connections;

    @Bean
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.replica")
    public DataSourceProperties replicaDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public HikariDataSource primaryDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(primaryDataSourceProperties().getDriverClassName())
                .url(primaryDataSourceProperties().getUrl())
                .username(primaryDataSourceProperties().getUsername())
                .password(primaryDataSourceProperties().getPassword())
                .build();
    }

    @Bean
    public HikariDataSource replicaDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .driverClassName(replicaDataSourceProperties().getDriverClassName())
                .url(replicaDataSourceProperties().getUrl())
                .username(replicaDataSourceProperties().getUsername())
                .password(replicaDataSourceProperties().getPassword())
                .build();
    }

    @Bean
    public DataSource routingDataSource(
            HikariDataSource primaryDataSource,
            HikariDataSource replicaDataSource
    ) {
        DatabaseConnectionMonitor monitor = new DatabaseConnectionMonitor(Map.of(
                "primary", primaryDataSource,
                "replica", replicaDataSource
        ));
        LoadBalancedRoutingDataSource routingDataSource = new LoadBalancedRoutingDataSource(monitor);
        routingDataSource.setTargetDataSources(Map.of(
                "primary", primaryDataSource,
                "replica", replicaDataSource
        ));
        routingDataSource.setDefaultTargetDataSource(primaryDataSource);
        routingDataSource.afterPropertiesSet();
        return routingDataSource;
    }

    @Primary
    @Bean
    public DataSource dataSource() {
        HikariDataSource primaryDataSource = primaryDataSource();
        primaryDataSource.setPoolName("HikariPool-User-Primary");
        primaryDataSource.setMaximumPoolSize(connections);
        dataSourceSetting(primaryDataSource);
        HikariDataSource replicaDataSource = replicaDataSource();
        replicaDataSource.setMaximumPoolSize(connections);
        replicaDataSource.setPoolName("HikariPool-User-Replica");
        dataSourceSetting(replicaDataSource);
        return new LazyConnectionDataSourceProxy(routingDataSource(primaryDataSource, replicaDataSource));
    }

    private void dataSourceSetting(HikariDataSource dataSource){
        dataSource.setMaximumPoolSize(connections);
        dataSource.setConnectionTimeout(2000);
        dataSource.setAllowPoolSuspension(true);
        dataSource.setLeakDetectionThreshold(10000);
        dataSource.setInitializationFailTimeout(2000);
    }
}