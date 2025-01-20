package com.portfolio.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

@Data
public class PortfolioSummaryDTO {
    private BigDecimal totalValue;
    private BigDecimal totalGainLoss;
    private BigDecimal totalGainLossPercentage;
    private String bestPerformingStock;
    private String worstPerformingStock;
    private Map<String, Double> sectorDistribution;
}