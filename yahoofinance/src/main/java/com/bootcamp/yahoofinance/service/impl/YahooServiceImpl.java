package com.bootcamp.yahoofinance.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.repository.StockListRepository;
import com.bootcamp.yahoofinance.service.YahooService;

@Service
public class YahooServiceImpl implements YahooService {

  @Autowired
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private StockListRepository stockListRepository;

  @Override
  public List<YahooDto> getYahoo() {

    List<String> stockList = stockListRepository.findAll().stream().map(e -> e.getSymbol()).collect(Collectors.toList());

    return this.yahooManager.getYahooDtos(this.restTemplate, stockList);
    
  }
}