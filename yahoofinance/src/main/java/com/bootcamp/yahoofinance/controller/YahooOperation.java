package com.bootcamp.yahoofinance.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.yahoofinance.DTO.StockDTO;

public interface YahooOperation {
  
  @GetMapping(value = "/stock")
  StockDTO getPrice(@RequestParam String stockCode);

}
