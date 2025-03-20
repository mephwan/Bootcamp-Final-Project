package com.bootcamp.yahoofinance.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockOHLCDTO;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface YahooOperation {
  
  @GetMapping(value = "/stock/m5")
  StockDTO getPrice(@RequestParam String stockCode) throws JsonProcessingException;

  @GetMapping(value = "/stock/d1")
  StockOHLCDTO getDayOHLC(@RequestParam String stockCode);

  @GetMapping(value = "/stock/w1")
  StockOHLCDTO getWeekOHLC(@RequestParam String stockCode);

  @GetMapping(value = "stock/mn")
  StockOHLCDTO getMonthOHLC(@RequestParam String stockCode);
}
