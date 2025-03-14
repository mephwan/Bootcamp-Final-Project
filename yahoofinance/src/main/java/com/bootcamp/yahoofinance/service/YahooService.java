package com.bootcamp.yahoofinance.service;

import com.bootcamp.yahoofinance.DTO.StockDTO;

public interface YahooService {

  StockDTO getPrice(String stockCode);
}
