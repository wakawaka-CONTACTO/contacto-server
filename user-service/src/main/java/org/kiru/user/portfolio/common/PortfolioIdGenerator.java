package org.kiru.user.portfolio.common;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PortfolioIdGenerator {
    public Long generatePortfolioId() {
        // ID 생성 로직
        return UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    }
}