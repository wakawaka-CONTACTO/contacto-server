package org.kiru.core.user.userPortfolioItem.domain;

public interface UserPortfolioItem {
    Long getId();
    int getSequence();
    Long getPortfolioId();
    Long getUserId();
    String getItemUrl();
    String getUserName();
}
