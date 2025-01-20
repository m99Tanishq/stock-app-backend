package com.portfolio.service;

import com.portfolio.domain.Portfolio;
import com.portfolio.domain.StockHolding;
import com.portfolio.domain.User;
import com.portfolio.dto.PortfolioDTO;
import com.portfolio.dto.PortfolioSummaryDTO;
import com.portfolio.dto.StockHoldingDTO;
import com.portfolio.exception.CustomException;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.StockHoldingRepository;
import com.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {
    private final PortfolioRepository portfolioRepository;
    private final StockHoldingRepository stockHoldingRepository;
    private final UserRepository userRepository;
    private final StockPriceService stockPriceService;

    @Transactional
    public PortfolioDTO createPortfolio(PortfolioDTO portfolioDTO) {
        User user = userRepository.findById(portfolioDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Portfolio portfolio = new Portfolio();
        portfolio.setName(portfolioDTO.getName());
        portfolio.setUser(user);

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // Add 5 random stocks to the portfolio
        List<String> randomStocks = stockPriceService.getRandomStocks(5);
        for (String ticker : randomStocks) {
            try {
                BigDecimal currentPrice = stockPriceService.getCurrentPrice(ticker);

                StockHolding holding = new StockHolding();
                holding.setTicker(ticker);
                holding.setStockName(ticker); // You might want to fetch the company name from another API
                holding.setQuantity(1); // As per requirement
                holding.setPurchasePrice(currentPrice);
                holding.setPurchaseDate(LocalDateTime.now());
                holding.setPortfolio(savedPortfolio);

                stockHoldingRepository.save(holding);
            } catch (Exception e) {
                log.error("Error adding stock {} to portfolio", ticker, e);
            }
        }

        return mapToDTO(portfolioRepository.findById(savedPortfolio.getId()).orElseThrow());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "portfolioSummaries", key = "#portfolioId")
    public PortfolioSummaryDTO getPortfolioSummary(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        List<StockHolding> holdings = stockHoldingRepository.findByPortfolioId(portfolioId);

        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        Map<String, BigDecimal> performanceMap = new HashMap<>();

        for (StockHolding holding : holdings) {
            BigDecimal currentPrice = stockPriceService.getCurrentPrice(holding.getTicker());
            BigDecimal quantity = new BigDecimal(holding.getQuantity());

            BigDecimal currentValue = currentPrice.multiply(quantity);
            BigDecimal cost = holding.getPurchasePrice().multiply(quantity);

            totalValue = totalValue.add(currentValue);
            totalCost = totalCost.add(cost);

            // Calculate individual stock performance
            BigDecimal performance = calculatePerformance(currentPrice, holding.getPurchasePrice());
            performanceMap.put(holding.getTicker(), performance);
        }

        return createSummaryDTO(totalValue, totalCost, performanceMap);
    }

    @Transactional
    @CacheEvict(value = "portfolioSummaries", key = "#portfolioId")
    public StockHoldingDTO updateStockHolding(Long portfolioId, Long holdingId, StockHoldingDTO dto) {
        StockHolding holding = stockHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found"));

        if (!holding.getPortfolio().getId().equals(portfolioId)) {
            throw new CustomException("Stock holding does not belong to this portfolio",
                    HttpStatus.BAD_REQUEST);
        }

        holding.setQuantity(dto.getQuantity());
        // Update other fields as needed

        StockHolding updatedHolding = stockHoldingRepository.save(holding);
        return mapToDTO(updatedHolding);
    }

    @Transactional
    public StockHoldingDTO addStock(StockHoldingDTO stockDTO) {
        Portfolio portfolio = portfolioRepository.findById(stockDTO.getPortfolioId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));

        StockHolding stock = new StockHolding();
        stock.setTicker(stockDTO.getTicker().toUpperCase());
        stock.setStockName(stockDTO.getStockName());
        stock.setQuantity(stockDTO.getQuantity());
        stock.setPurchasePrice(stockDTO.getPurchasePrice());
        stock.setPurchaseDate(LocalDateTime.now());
        stock.setPortfolio(portfolio);

        StockHolding savedStock = stockHoldingRepository.save(stock);
        return mapToDTO(savedStock);
    }

    @Transactional(readOnly = true)
    public PortfolioDTO getPortfolio(Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found"));
        return mapToDTO(portfolio);
    }

    @Transactional
    @CacheEvict(value = "portfolioSummaries", key = "#portfolioId")
    public void deleteStockHolding(Long portfolioId, Long holdingId) {
        StockHolding holding = stockHoldingRepository.findById(holdingId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found"));

        if (!holding.getPortfolio().getId().equals(portfolioId)) {
            throw new CustomException("Stock holding does not belong to this portfolio",
                    HttpStatus.BAD_REQUEST);
        }

        stockHoldingRepository.delete(holding);
    }

    @Transactional(readOnly = true)
    public List<PortfolioDTO> getUserPortfolios(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        return portfolioRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal calculatePerformance(BigDecimal currentPrice, BigDecimal purchasePrice) {
        return currentPrice.subtract(purchasePrice)
                .divide(purchasePrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal(100));
    }

    private PortfolioSummaryDTO createSummaryDTO(
            BigDecimal totalValue,
            BigDecimal totalCost,
            Map<String, BigDecimal> performanceMap) {

        PortfolioSummaryDTO summary = new PortfolioSummaryDTO();
        summary.setTotalValue(totalValue);
        summary.setTotalGainLoss(totalValue.subtract(totalCost));

        if (totalCost.compareTo(BigDecimal.ZERO) > 0) {
            summary.setTotalGainLossPercentage(
                    totalValue.subtract(totalCost)
                            .divide(totalCost, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100))
            );
        }

        if (!performanceMap.isEmpty()) {
            Map.Entry<String, BigDecimal> bestPerforming = performanceMap.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElseThrow();

            Map.Entry<String, BigDecimal> worstPerforming = performanceMap.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .orElseThrow();

            summary.setBestPerformingStock(String.format("%s (%.2f%%)",
                    bestPerforming.getKey(), bestPerforming.getValue()));
            summary.setWorstPerformingStock(String.format("%s (%.2f%%)",
                    worstPerforming.getKey(), worstPerforming.getValue()));
        }

        return summary;
    }

    private PortfolioDTO mapToDTO(Portfolio portfolio) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setId(portfolio.getId());
        dto.setName(portfolio.getName());
        dto.setUserId(portfolio.getUser().getId());

        if (portfolio.getHoldings() != null) {
            dto.setHoldings(portfolio.getHoldings().stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList()));

            BigDecimal totalValue = portfolio.getHoldings().stream()
                    .map(holding -> {
                        BigDecimal currentPrice = stockPriceService.getCurrentPrice(holding.getTicker());
                        return currentPrice.multiply(new BigDecimal(holding.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            dto.setTotalValue(totalValue);
        }

        return dto;
    }

    private StockHoldingDTO mapToDTO(StockHolding holding) {
        StockHoldingDTO dto = new StockHoldingDTO();
        dto.setId(holding.getId());
        dto.setTicker(holding.getTicker());
        dto.setStockName(holding.getStockName());
        dto.setQuantity(holding.getQuantity());
        dto.setPurchasePrice(holding.getPurchasePrice());
        dto.setPurchaseDate(holding.getPurchaseDate());
        dto.setPortfolioId(holding.getPortfolio().getId());

        BigDecimal currentPrice = stockPriceService.getCurrentPrice(holding.getTicker());
        dto.setCurrentPrice(currentPrice);
        dto.setTotalValue(currentPrice.multiply(new BigDecimal(holding.getQuantity())));

        return dto;
    }

    @Transactional
    public void deleteStock(Long stockId) {
        StockHolding stock = stockHoldingRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found"));
        stockHoldingRepository.delete(stock);
    }

    @Transactional
    public StockHoldingDTO updateStock(Long stockId, StockHoldingDTO stockDTO) {
        StockHolding stock = stockHoldingRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock holding not found"));

        stock.setQuantity(stockDTO.getQuantity());
        stock.setPurchasePrice(stockDTO.getPurchasePrice());

        StockHolding updatedStock = stockHoldingRepository.save(stock);
        return mapToDTO(updatedStock);
    }
}