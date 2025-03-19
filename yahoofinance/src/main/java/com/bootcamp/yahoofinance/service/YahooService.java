package com.bootcamp.yahoofinance.service;

import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface YahooService {

  StockDTO getPrice(String stockCode) throws JsonProcessingException;

}
