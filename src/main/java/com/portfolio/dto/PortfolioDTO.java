package com.portfolio.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
public class PortfolioDTO {
    private Long id;

    @NotBlank(message = "Portfolio name is required")
    private String name;
    private Long userId;
    private BigDecimal totalValue;
    private List<StockHoldingDTO> holdings;
}
