package com.bootcamp.finalproject_frontend.controller.impl;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.finalproject_frontend.controller.LineChartOperation;
import com.bootcamp.finalproject_frontend.dto.LinePointDTO;
import com.bootcamp.finalproject_frontend.dto.mapper.ChartMapper;
import com.bootcamp.finalproject_frontend.model.LinePoint;
import com.bootcamp.finalproject_frontend.model.dto.StockPriceDto;

@RestController
@RequestMapping(value = "/v1")
public class LineChartController implements LineChartOperation {
  @Autowired
  private ChartMapper chartMapper;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  @GetMapping("/chart/line")
  public List<LinePointDTO> getLineChart(String interval, String symbol) {

    List<LinePoint> pricePoints = switch (LinePoint.TYPE.of(interval)) {
      case FIVE_MIN -> getPricePointByFiveMinute(symbol);
    };
    return pricePoints.stream() //
        .map(e -> this.chartMapper.map(e)) //
        .collect(Collectors.toList());
  }

  private List<LinePoint> getPricePointByFiveMinute(String symbol) {

    StockPriceDto stockpriceDto = this.restTemplate.getForObject(
        "http://localhost:8080/stock/m5?stockCode=" + symbol, StockPriceDto.class);

    return stockpriceDto.getData().stream()
        .map(e -> new LinePoint(e.getMarketDateTime().getYear(),
            e.getMarketDateTime().getMonthValue(),
            e.getMarketDateTime().getDayOfMonth(),
            e.getMarketDateTime().getHour(), 
            e.getMarketDateTime().getMinute(),
            e.getRegularMarketPrice()))
        .collect(Collectors.toList());

  }
}
