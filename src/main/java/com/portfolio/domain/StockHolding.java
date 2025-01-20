package com.portfolio.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_holdings")
public class StockHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticker;

    @Column(nullable = false)
    private String stockName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal purchasePrice;

    @Column(nullable = false)
    private LocalDateTime purchaseDate;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    // Current price will be fetched from external API
    @Transient
    private BigDecimal currentPrice;

    @Transient
    private BigDecimal totalValue;
}