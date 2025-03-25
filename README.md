# Stock Market Visualization System

## Stock Chart Example
M5 chart
https://github.com/mephwan/Bootcamp-Final-Project/tree/main/Frontend-StockBoard/src/pic/m5.jpg

D1 chart
https://github.com/mephwan/Bootcamp-Final-Project/tree/main/Frontend-StockBoard/src/pic/d1.jpg

W1 chart
https://github.com/mephwan/Bootcamp-Final-Project/tree/main/Frontend-StockBoard/src/pic/w1.jpg

MN chart
https://github.com/mephwan/Bootcamp-Final-Project/tree/main/Frontend-StockBoard/src/pic/mn.jpg


A full-stack application for visualizing stock market data with real-time updates and multiple chart types.

**GitHub Repository**: [https://github.com/mephwan/Bootcamp-Final-Project](https://github.com/mephwan/Bootcamp-Final-Project)

## Features

### Backend (Java Spring Boot)
- Fetches real-time stock data from Yahoo Finance API
- Supports multiple time intervals:
  - 5-minute intervals (M5)
  - Daily intervals (D1)
  - Weekly intervals (W1)
  - Monthly intervals (MN)
- RESTful API endpoints for frontend consumption
- Caching mechanism using Redis
- Database integration with PostgreSQL
- Jackson serialization for Java 8 Date/Time API

### Frontend (JavaScript)
- Interactive candlestick charts (D1/W1/MN)
- Line charts for 5-minute intervals
- Moving Average (MA) indicators
- Auto-refresh every 10 seconds
- Symbol selector with major tech stocks
- Responsive dark-themed UI
- Timeframe selection (5M/D/W/M)


## Project Structure
Backend (Spring Boot)
src/
??? main/
?   ??? java/
?   ?   ??? com/stockapp/
?   ?       ??? controller/     # API controllers
?   ?       ??? service/        # Business logic
?   ?       ??? repository/     # Database operations
?   ?       ??? model/          # Data entities
?   ?       ??? config/         # Spring configurations
?   ??? resources/
?       ??? application.properties


Frontend
frontend/
??? index.html          # Main interface
??? chart.js            # Chart logic
??? styles.css          # UI styling
??? lightweight-charts  # Charting library