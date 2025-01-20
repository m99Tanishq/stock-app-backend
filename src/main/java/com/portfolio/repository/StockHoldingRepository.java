package com.portfolio.repository;

import com.portfolio.domain.StockHolding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StockHoldingRepository extends JpaRepository<StockHolding, Long> {
    List<StockHolding> findByPortfolioId(Long portfolioId);

    @Query("SELECT DISTINCT sh.ticker FROM StockHolding sh WHERE sh.portfolio.id = :portfolioId")
    List<String> findDistinctTickersByPortfolioId(Long portfolioId);
}
