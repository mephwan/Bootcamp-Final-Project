package com.bootcamp.yahoofinance.service;

import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockOHLCDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface YahooService {

  StockDTO getPrice(String stockCode) throws JsonProcessingException;

  StockOHLCDTO getDayOHLC(String stockCode);

  StockOHLCDTO getWeekOHLC(String stockCode);

  StockOHLCDTO getMonthOHLC(String stockCode);
}
