package com.portfolio.controller;

import com.portfolio.dto.StockHoldingDTO;
import com.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StockHoldingController {
    private final PortfolioService portfolioService;

    @PostMapping
    public ResponseEntity<StockHoldingDTO> addStock(@Valid @RequestBody StockHoldingDTO stockDTO) {
        return ResponseEntity.ok(portfolioService.addStock(stockDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockHoldingDTO> updateStock(
            @PathVariable Long id,
            @Valid @RequestBody StockHoldingDTO stockDTO) {
        return ResponseEntity.ok(portfolioService.updateStock(id, stockDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStock(@PathVariable Long id) {
        portfolioService.deleteStock(id);
        return ResponseEntity.noContent().build();
    }
}