-- Create database --
CREATE DATABASE IF NOT EXISTS portfolio_tracker;
USE portfolio_tracker;

-- Users table --
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Portfolios table --
CREATE TABLE IF NOT EXISTS portfolios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Stock holdings table --
CREATE TABLE IF NOT EXISTS stock_holdings (
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

-- Handle Index: idx_user_username --
SELECT IF(
    EXISTS(
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = 'portfolio_tracker'
          AND table_name = 'users'
          AND index_name = 'idx_user_username'
    ),
    'SELECT ''Index idx_user_username exists.'';',
    'CREATE INDEX idx_user_username ON users(username)'
) INTO @query1;
PREPARE stmt1 FROM @query1;
EXECUTE stmt1;
DEALLOCATE PREPARE stmt1;

-- Handle Index: idx_portfolio_user --
SELECT IF(
    EXISTS(
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = 'portfolio_tracker'
          AND table_name = 'portfolios'
          AND index_name = 'idx_portfolio_user'
    ),
    'SELECT ''Index idx_portfolio_user exists.'';',
    'CREATE INDEX idx_portfolio_user ON portfolios(user_id)'
) INTO @query2;
PREPARE stmt2 FROM @query2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Handle Index: idx_holding_portfolio --
SELECT IF(
    EXISTS(
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = 'portfolio_tracker'
          AND table_name = 'stock_holdings'
          AND index_name = 'idx_holding_portfolio'
    ),
    'SELECT ''Index idx_holding_portfolio exists.'';',
    'CREATE INDEX idx_holding_portfolio ON stock_holdings(portfolio_id)'
) INTO @query3;
PREPARE stmt3 FROM @query3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- Handle Index: idx_holding_ticker --
SELECT IF(
    EXISTS(
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = 'portfolio_tracker'
          AND table_name = 'stock_holdings'
          AND index_name = 'idx_holding_ticker'
    ),
    'SELECT ''Index idx_holding_ticker exists.'';',
    'CREATE INDEX idx_holding_ticker ON stock_holdings(ticker)'
) INTO @query4;
PREPARE stmt4 FROM @query4;
EXECUTE stmt4;
DEALLOCATE PREPARE stmt4;
