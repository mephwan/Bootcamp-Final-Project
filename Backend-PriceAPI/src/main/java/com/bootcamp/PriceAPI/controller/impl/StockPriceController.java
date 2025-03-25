package com.bootcamp.PriceAPI.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.bootcamp.PriceAPI.DTO.StockDataDTO;
import com.bootcamp.PriceAPI.DTO.StockOHLCDTO;
import com.bootcamp.PriceAPI.controller.StockPriceOperation;
import com.bootcamp.PriceAPI.model.ApiResp;
import com.bootcamp.PriceAPI.service.StockPriceService;
import com.fasterxml.jackson.core.JsonProcessingException;

@RestController
public class StockPriceController implements StockPriceOperation {
  @Autowired
  private StockPriceService yahooService;

  @Override
  public ApiResp<StockDataDTO> getPrice(String stockCode) throws JsonProcessingException {
    return this.yahooService.getPrice(stockCode);
  }

  @Override
  public ApiResp<StockOHLCDTO> getDayOHLC(String stockCode) throws JsonProcessingException {
    return this.yahooService.getDayOHLC(stockCode);
  }

  @Override
  public ApiResp<StockOHLCDTO> getWeekOHLC(String stockCode) throws JsonProcessingException {
    return this.yahooService.getWeekOHLC(stockCode);
  }

  @Override
  public ApiResp<StockOHLCDTO> getMonthOHLC(String stockCode) throws JsonProcessingException {
    return this.yahooService.getMonthOHLC(stockCode);
  }
}
