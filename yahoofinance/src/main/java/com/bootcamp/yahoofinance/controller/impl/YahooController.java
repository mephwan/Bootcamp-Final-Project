package com.bootcamp.yahoofinance.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.controller.YahooOperation;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.bootcamp.yahoofinance.service.YahooService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class YahooController implements YahooOperation {
  @Autowired
  private YahooService yahooService;

  @Override
  public StockDTO getPrice(String stockCode) throws JsonProcessingException {
    return this.yahooService.getPrice(stockCode);
  }

  @Override
  public YahooOHLCDto geOhlcDto() {
    return this.yahooService.getOhlcDto();
  }
}
