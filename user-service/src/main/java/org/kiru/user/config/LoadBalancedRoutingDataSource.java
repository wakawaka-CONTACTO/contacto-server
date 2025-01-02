package org.kiru.user.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

@Slf4j
@Profile({"docker", "local"})
class LoadBalancedRoutingDataSource extends AbstractRoutingDataSource {
    private final DatabaseConnectionMonitor monitor;

    public LoadBalancedRoutingDataSource(DatabaseConnectionMonitor monitor) {
        this.monitor = monitor;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        String key = monitor.getDataSourceKey();
        log.info("determineCurrentLookupKey: {}", key);
        return key;
    }
}