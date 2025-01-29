package org.kiru.core.jwt;

import java.util.Date;

public interface TokenGenerator {
    String generateToken(final long userId, final String email, Date now);
    Date generateExpirationDate(final Date now);
}
