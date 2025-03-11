package com.bootcamp.yahoofinance.controller.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import com.bootcamp.yahoofinance.controller.YahooOperation;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.service.YahooService;

@RestController
public class YahooController implements YahooOperation {
  @Autowired
  private YahooService yahooService;
  
  @Override
  public List<YahooDto> getYahoo() {
    return this.yahooService.getYahoo();
  }
}
