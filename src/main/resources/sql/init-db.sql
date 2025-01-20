-- Create database--
CREATE DATABASE IF NOT EXISTS portfolio_tracker;
USE portfolio_tracker;

-- Users table--
CREATE TABLE  users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Portfolios table--
CREATE TABLE  portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Stock holdings table--
CREATE TABLE  stock_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    ticker VARCHAR(10) NOT NULL,
    stock_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    purchase_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id)
);

CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_portfolio_user ON portfolios(user_id);
CREATE INDEX idx_holding_portfolio ON stock_holdings(portfolio_id);
CREATE INDEX idx_holding_ticker ON stock_holdings(ticker);