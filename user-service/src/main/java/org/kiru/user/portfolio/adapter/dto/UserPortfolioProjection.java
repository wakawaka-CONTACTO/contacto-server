package org.kiru.user.portfolio.adapter.dto;

public interface UserPortfolioProjection {
    Long getPortfolioId();
    Long getUserId();
    String getUsername();
    String getPortfolioImageUrl();
}
