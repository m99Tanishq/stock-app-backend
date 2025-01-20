package com.portfolio.service;

import com.portfolio.domain.User;
import com.portfolio.domain.Portfolio;
import com.portfolio.domain.StockHolding;
import com.portfolio.dto.PortfolioDTO;
import com.portfolio.dto.StockHoldingDTO;
import com.portfolio.exception.ResourceNotFoundException;
import com.portfolio.repository.PortfolioRepository;
import com.portfolio.repository.StockHoldingRepository;
import com.portfolio.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {
    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockHoldingRepository stockHoldingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockPriceService stockPriceService;

    @InjectMocks
    private PortfolioService portfolioService;

    private User testUser;
    private Portfolio testPortfolio;
    private StockHolding testStockHolding;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testPortfolio = new Portfolio();
        testPortfolio.setId(1L);
        testPortfolio.setName("Test Portfolio");
        testPortfolio.setUser(testUser);

        testStockHolding = new StockHolding();
        testStockHolding.setId(1L);
        testStockHolding.setTicker("AAPL");
        testStockHolding.setStockName("Apple Inc");
        testStockHolding.setQuantity(10);
        testStockHolding.setPurchasePrice(new BigDecimal("150.00"));
        testStockHolding.setPortfolio(testPortfolio);
    }

    @Test
    void createPortfolio_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(testPortfolio);

        PortfolioDTO portfolioDTO = new PortfolioDTO();
        portfolioDTO.setName("Test Portfolio");
        portfolioDTO.setUserId(1L);

        PortfolioDTO result = portfolioService.createPortfolio(portfolioDTO);

        assertNotNull(result);
        assertEquals("Test Portfolio", result.getName());
        assertEquals(1L, result.getUserId());
    }

    @Test
    void getPortfolioSummary_Success() {
        when(portfolioRepository.findById(1L)).thenReturn(Optional.of(testPortfolio));
        when(stockHoldingRepository.findByPortfolioId(1L))
                .thenReturn(Arrays.asList(testStockHolding));
        when(stockPriceService.getCurrentPrice("AAPL"))
                .thenReturn(new BigDecimal("160.00"));

        var summary = portfolioService.getPortfolioSummary(1L);

        assertNotNull(summary);
        assertTrue(summary.getTotalValue().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void getPortfolioSummary_NotFound() {
        when(portfolioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            portfolioService.getPortfolioSummary(99L);
        });
    }
}

// Add UserService Test
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("newuser");
        userDTO.setEmail("new@example.com");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername(userDTO.getUsername());
        savedUser.setEmail(userDTO.getEmail());

        when(userRepository.existsByUsername(userDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(userDTO.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDTO result = userService.createUser(userDTO);

        assertNotNull(result);
        assertEquals(userDTO.getUsername(), result.getUsername());
        assertEquals(userDTO.getEmail(), result.getEmail());
    }
}