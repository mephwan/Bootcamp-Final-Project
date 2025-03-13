package com.bootcamp.yahoofinance.service;

import java.util.List;
import com.bootcamp.yahoofinance.DTO.StockDTO;

public interface YahooService {

  List<StockDTO> getPrice(String stockCode);
}
