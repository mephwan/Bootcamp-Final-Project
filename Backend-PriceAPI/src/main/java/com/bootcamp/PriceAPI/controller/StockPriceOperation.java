package com.bootcamp.PriceAPI.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.PriceAPI.DTO.StockDataDTO;
import com.bootcamp.PriceAPI.DTO.StockOHLCDTO;
import com.bootcamp.PriceAPI.model.ApiResp;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface StockPriceOperation {
  
  @GetMapping(value = "/stock/m5")
  ApiResp<StockDataDTO> getPrice(@RequestParam String stockCode) throws JsonProcessingException;

  @GetMapping(value = "/stock/d1")
  ApiResp<StockOHLCDTO> getDayOHLC(@RequestParam String stockCode) throws JsonProcessingException;

  @GetMapping(value = "/stock/w1")
  ApiResp<StockOHLCDTO> getWeekOHLC(@RequestParam String stockCode) throws JsonProcessingException;

  @GetMapping(value = "stock/mn")
  ApiResp<StockOHLCDTO> getMonthOHLC(@RequestParam String stockCode) throws JsonProcessingException;
}
