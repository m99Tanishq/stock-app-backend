package com.portfolio.controller;

import com.portfolio.dto.PortfolioDTO;
import com.portfolio.dto.PortfolioSummaryDTO;
import com.portfolio.dto.StockHoldingDTO;
import com.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PortfolioController {
    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<PortfolioDTO> createPortfolio(@Valid @RequestBody PortfolioDTO portfolioDTO) {
        return ResponseEntity.ok(portfolioService.createPortfolio(portfolioDTO));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PortfolioDTO>> getUserPortfolios(@PathVariable Long userId) {
        return ResponseEntity.ok(portfolioService.getUserPortfolios(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PortfolioDTO> getPortfolio(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolio(id));
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<PortfolioSummaryDTO> getPortfolioSummary(@PathVariable Long id) {
        return ResponseEntity.ok(portfolioService.getPortfolioSummary(id));
    }
}
