package com.bootcamp.yahoofinance.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import com.bootcamp.yahoofinance.model.YahooDto;

public interface YahooOperation {
  
  @GetMapping(value = "/yahoo")
  List<YahooDto> getYahoo();
}
