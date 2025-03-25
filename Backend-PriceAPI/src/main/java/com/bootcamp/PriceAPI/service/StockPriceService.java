package com.bootcamp.PriceAPI.service;

import com.bootcamp.PriceAPI.DTO.StockDataDTO;
import com.bootcamp.PriceAPI.DTO.StockOHLCDTO;
import com.bootcamp.PriceAPI.model.ApiResp;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface StockPriceService {

  ApiResp<StockDataDTO> getPrice(String stockCode) throws JsonProcessingException;

  ApiResp<StockOHLCDTO> getDayOHLC(String stockCode) throws JsonProcessingException;

  ApiResp<StockOHLCDTO> getWeekOHLC(String stockCode) throws JsonProcessingException;

  ApiResp<StockOHLCDTO> getMonthOHLC(String stockCode) throws JsonProcessingException;
}
