package org.kiru.user.portfolio.common;

import java.util.UUID;
import org.kiru.core.exception.ContactoException;
import org.kiru.core.exception.code.FailureCode;
import org.springframework.stereotype.Component;

@Component
public class PortfolioIdGenerator {
    public Long generatePortfolioId() {
        try {
            UUID uuid = UUID.randomUUID();
            return Math.abs(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
        } catch (Exception e) {
            throw new ContactoException(FailureCode.PORTFOLIO_ID_GENERATION_FAILED);
        }
    }
}