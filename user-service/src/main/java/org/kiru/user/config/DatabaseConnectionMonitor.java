package org.kiru.user.config;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Configuration(proxyBeanMethods = false)
@Slf4j
@Profile({"docker", "local"})
class DatabaseConnectionMonitor {
    private final Map<String, HikariDataSource> dataSources;
    private final Map<String, Boolean> connectionHealthCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();

    public DatabaseConnectionMonitor(Map<String, HikariDataSource> dataSources) {
        this.dataSources = dataSources;
        healthCheckExecutor.scheduleAtFixedRate(this::updateConnectionHealthCache, 0, 5, TimeUnit.SECONDS);
    }

    private void updateConnectionHealthCache() {
        dataSources.forEach((key, dataSource) -> {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
            boolean isHealthy = false;
            try {
                isHealthy =
                        performHealthCheck(dataSource) &&
                                poolMXBean.getTotalConnections() > 0 &&
                                poolMXBean.getActiveConnections() < poolMXBean.getIdleConnections();
            } catch (Exception e) {
                log.error("Health check failed for datasource {}: {}", key, e.getMessage());
            }
            connectionHealthCache.put(key, isHealthy);
        });
    }

    public String getDataSourceKey() {
        boolean isReadOnly = isTransactionReadOnly();
        Map<String, HikariDataSource> healthyDataSources = getHealthyDataSources();
        if (!isReadOnly) {
            return "primary";
        } else if (healthyDataSources.isEmpty()) {
            return getLeastConnectionByDataSource(dataSources);
        }
        return getLeastConnectionByDataSource(healthyDataSources);
    }

    private boolean isTransactionReadOnly() {
        try {
            return TransactionAspectSupport.currentTransactionStatus().isReadOnly();
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, HikariDataSource> getHealthyDataSources() {
        return dataSources.entrySet().stream()
                .filter(entry -> connectionHealthCache.getOrDefault(entry.getKey(), false))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private String getLeastConnectionByDataSource(Map<String, HikariDataSource> dataSources) {
        return dataSources.entrySet().stream()
                .min(Comparator.comparingInt(entry -> {
                    HikariPoolMXBean poolMXBean = entry.getValue().getHikariPoolMXBean();
                    return poolMXBean.getThreadsAwaitingConnection();
                }))
                .map(Map.Entry::getKey)
                .orElse("primary");
    }

    private boolean performHealthCheck(HikariDataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            log.error("Database connection health check failed", e);
            return false;
        }
    }

    public void logConnectionStats() {
        dataSources.forEach((key, dataSource) -> {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
//            log.debug("DataSource {}: Active={}, Idle={}, Total={}, Healthy={}",
//                    key,
//                    poolMXBean.getActiveConnections(),
//                    poolMXBean.getIdleConnections(),
//                    poolMXBean.getTotalConnections(),
//                    connectionHealthCache.getOrDefault(key, false));
        });
    }

    @PreDestroy
    public void shutdown() {
        healthCheckExecutor.shutdown();
    }
}