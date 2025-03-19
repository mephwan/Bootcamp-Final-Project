package com.bootcamp.yahoofinance.service;

import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface YahooService {

  StockDTO getPrice(String stockCode) throws JsonProcessingException;

  YahooOHLCDto getOhlcDto();
}
