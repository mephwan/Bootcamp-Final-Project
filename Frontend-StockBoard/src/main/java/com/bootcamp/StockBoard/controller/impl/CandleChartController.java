package com.bootcamp.StockBoard.controller.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.StockBoard.controller.CandleChartOperation;
import com.bootcamp.StockBoard.dto.CandleStickDTO;
import com.bootcamp.StockBoard.dto.mapper.ChartMapper;
import com.bootcamp.StockBoard.model.CandleStick;
import com.bootcamp.StockBoard.model.dto.StockOHLCDto;

@RestController
@RequestMapping(value = "/v1")
public class CandleChartController implements CandleChartOperation {
  @Autowired
  private ChartMapper chartMapper;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public List<CandleStickDTO> getCandleChart(String interval, String symbol) {
    List<CandleStick> candleSticks = switch (CandleStick.TYPE.of(interval)) {
      case DAY -> getCandlesByDay(symbol);
      case WEEK -> getCandlesByWeek(symbol);
      case MONTH -> getCandlesByMonth(symbol);
    };
    return candleSticks.stream().map(e -> this.chartMapper.map(e))
        .collect(Collectors.toList());
  }

  private List<CandleStick> getCandlesByDay(String symbol) {

    String url = "http://localhost:8080/stock/d1?stockCode=" + symbol;

    StockOHLCDto stockOHLCDTO = this.restTemplate.getForObject(url, StockOHLCDto.class);
    
    return stockOHLCDTO.getBody().getData().stream().map(e -> 
      new CandleStick(e.getMarketDate().getYear(), 
      e.getMarketDate().getMonthValue(), 
      e.getMarketDate().getDayOfMonth(), 
      e.getOpen(), 
      e.getHigh(), 
      e.getLow(), 
      e.getClose()))
      .collect(Collectors.toList());
        //new CandleStick(2024, 1, 10, 105.0, 111.0, 104.0, 111.0) // 2024-01-10
    
  }

  private List<CandleStick> getCandlesByWeek(String symbol) {

    String url = "http://localhost:8080/stock/w1?stockCode=" + symbol;

    StockOHLCDto stockOHLCDTO = this.restTemplate.getForObject(url, StockOHLCDto.class);
    
    return stockOHLCDTO.getBody().getData().stream().map(e -> 
      new CandleStick(e.getMarketDate().getYear(), 
      e.getMarketDate().getMonthValue(), 
      e.getMarketDate().getDayOfMonth(), 
      e.getOpen(), 
      e.getHigh(), 
      e.getLow(), 
      e.getClose()))
      .collect(Collectors.toList());
        //new CandleStick(2024, 1, 10, 105.0, 111.0, 104.0, 111.0) // 2024-01-10
    
  }

  private List<CandleStick> getCandlesByMonth(String symbol) {

    String url = "http://localhost:8080/stock/mn?stockCode=" + symbol;

    StockOHLCDto stockOHLCDTO = this.restTemplate.getForObject(url, StockOHLCDto.class);
    
    return stockOHLCDTO.getBody().getData().stream().map(e -> 
      new CandleStick(e.getMarketDate().getYear(), 
      e.getMarketDate().getMonthValue(), 
      e.getMarketDate().getDayOfMonth(), 
      e.getOpen(), 
      e.getHigh(), 
      e.getLow(), 
      e.getClose()))
      .collect(Collectors.toList());
        //new CandleStick(2024, 1, 10, 105.0, 111.0, 104.0, 111.0) // 2024-01-10
    
  }
}
