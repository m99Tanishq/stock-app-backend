package com.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.exception.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${stock.api.base-url}")
    private String baseUrl;

    @Value("${stock.api.key}")
    private String apiKey;

    // Cache for stock prices with 15-minute expiry
    private final Map<String, CachedPrice> priceCache = new ConcurrentHashMap<>();

    @Cacheable(value = "stockPrices", key = "#ticker")
    public BigDecimal getCurrentPrice(String ticker) {
        try {
            // Check cache first
            CachedPrice cachedPrice = priceCache.get(ticker);
            if (cachedPrice != null && !cachedPrice.isExpired()) {
                log.debug("Cache hit for ticker: {}", ticker);
                return cachedPrice.getPrice();
            }

            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                    baseUrl, ticker, apiKey);

            String response = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(response);

            if (rootNode.has("Global Quote")) {
                JsonNode quoteNode = rootNode.get("Global Quote");
                BigDecimal price = new BigDecimal(quoteNode.get("05. price").asText());

                // Update cache
                priceCache.put(ticker, new CachedPrice(price));
                return price;
            } else if (rootNode.has("Note")) {
                // API rate limit reached
                log.warn("API rate limit reached for ticker: {}", ticker);
                throw new CustomException("API rate limit reached. Please try again later.",
                        HttpStatus.TOO_MANY_REQUESTS);
            } else {
                throw new CustomException("Invalid stock ticker: " + ticker,
                        HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error fetching stock price for ticker: {}", ticker, e);
            throw new CustomException("Error fetching stock price: " + e.getMessage(),
                    HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    public List<String> getRandomStocks(int count) {
        // List of popular stock tickers
        List<String> popularStocks = Arrays.asList(
                "AAPL", "MSFT", "GOOGL", "AMZN", "META",
                "TSLA", "NVDA", "JPM", "BAC", "DIS"
        );

        if (count > popularStocks.size()) {
            throw new CustomException("Requested count exceeds available stocks",
                    HttpStatus.BAD_REQUEST);
        }

        List<String> stocksCopy = new ArrayList<>(popularStocks);
        Collections.shuffle(stocksCopy);
        return stocksCopy.subList(0, count);
    }

    private static class CachedPrice {
        @Getter
        private final BigDecimal price;
        private final long timestamp;
        private static final long CACHE_DURATION = TimeUnit.MINUTES.toMillis(15);

        public CachedPrice(BigDecimal price) {
            this.price = price;
            this.timestamp = System.currentTimeMillis();
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - timestamp > CACHE_DURATION;
        }
    }
}