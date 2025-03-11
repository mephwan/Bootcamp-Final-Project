package com.bootcamp.yahoofinance.service;

import java.util.List;
import com.bootcamp.yahoofinance.model.YahooDto;

public interface YahooService {
  

  List<YahooDto> getYahoo();
}
