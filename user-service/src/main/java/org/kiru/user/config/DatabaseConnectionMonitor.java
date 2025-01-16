package org.kiru.user.config;

import static org.springframework.transaction.support.TransactionSynchronizationManager.isCurrentTransactionReadOnly;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import jakarta.annotation.PreDestroy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration(proxyBeanMethods = false)
@Slf4j
@Profile({"docker", "local"})
class DatabaseConnectionMonitor {
    private final Map<String, HikariDataSource> dataSources;
    private final Map<String, Boolean> connectionHealthCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService asyncExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final List<String> keys = new ArrayList<>(List.of("primary", "secondary"));

    public DatabaseConnectionMonitor(Map<String, HikariDataSource> dataSources) {
        this.dataSources = dataSources;
        healthCheckExecutor.scheduleAtFixedRate(this::updateConnectionHealthCache, 0, 5, TimeUnit.SECONDS);
    }

    private void updateConnectionHealthCache() {
        List<CompletableFuture<Void>> tasks = dataSources.entrySet().stream()
                .map(entry -> CompletableFuture.runAsync(() -> {
                    String key = entry.getKey();
                    HikariDataSource dataSource = entry.getValue();
                    boolean isHealthy = checkHealth(dataSource, key);
                    connectionHealthCache.put(key, isHealthy);
                }, asyncExecutor))
                .toList();
        tasks.forEach(CompletableFuture::join);
    }

    private boolean checkHealth(HikariDataSource dataSource, String dataSourceKey) {
        try {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
            boolean canConnect = performHealthCheck(dataSource);
            // 예시 조건: totalConnections > 0, active < idle
            boolean poolCondition = (poolMXBean.getTotalConnections() > 0)
                    && (poolMXBean.getActiveConnections() < poolMXBean.getIdleConnections());
            return canConnect && poolCondition;
        } catch (Exception e) {
            log.error("Health check failed for datasource {}: {}", dataSourceKey, e.getMessage());
            return false;
        }
    }

    public String getDataSourceKey() {
        boolean isReadOnly = isCurrentTransactionReadOnly();
        log.info("Is current transaction read-only: {}", isCurrentTransactionReadOnly());
        if (!isReadOnly) {
            return "primary";
        }
        return dataSources.entrySet().parallelStream()
                .filter(entry -> Boolean.TRUE.equals(connectionHealthCache.getOrDefault(entry.getKey(), false)))
                .sorted(Comparator.comparingInt(e -> e.getValue().getHikariPoolMXBean().getActiveConnections()))
                .map(Map.Entry::getKey)
                .findAny()
                .orElseGet(() -> {
                    Collections.shuffle(keys);
                    String randomKey = keys.getFirst();
                    log.warn("No healthy datasource found, falling back to random datasource: {}", randomKey);
                    return randomKey;
                });
    }

    private boolean performHealthCheck(HikariDataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            return (conn != null && !conn.isClosed());
        } catch (SQLException e) {
            return false;
        }
    }

    public void logConnectionStats() {
        dataSources.forEach((key, dataSource) -> {
            HikariPoolMXBean poolMXBean = dataSource.getHikariPoolMXBean();
        });
    }

    @PreDestroy
    public void shutdown() {
        healthCheckExecutor.shutdown();
        asyncExecutor.shutdown();
    }
}