package com.bootcamp.yahoofinance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface YahooOperation {
  
  @GetMapping(value = "/stock")
  StockDTO getPrice(@RequestParam String stockCode) throws JsonProcessingException;

  @GetMapping(value = "/ohlc")
  YahooOHLCDto geOhlcDto();

}
