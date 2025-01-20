package com.portfolio.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockHoldingDTO {
    private Long id;

    @NotBlank(message = "Ticker symbol is required")
    private String ticker;

    @NotBlank(message = "Stock name is required")
    private String stockName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Purchase price is required")
    private BigDecimal purchasePrice;

    private LocalDateTime purchaseDate;
    private Long portfolioId;
    private BigDecimal currentPrice;
    private BigDecimal totalValue;
}