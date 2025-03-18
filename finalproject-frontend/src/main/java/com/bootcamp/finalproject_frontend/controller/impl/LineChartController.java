package com.bootcamp.finalproject_frontend.controller.impl;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
  public List<LinePointDTO> getLineChart(String interval) {
    List<LinePoint> pricePoints = switch (LinePoint.TYPE.of(interval)) {
      case FIVE_MIN -> getPricePointByFiveMinute();
    };
    return pricePoints.stream() //
        .map(e -> this.chartMapper.map(e)) //
        .collect(Collectors.toList());
  }

  private List<LinePoint> getPricePointByFiveMinute() {

    StockPriceDto stockpriceDto = this.restTemplate.getForObject(
        "http://localhost:8080/stock?stockCode=0005.HK", StockPriceDto.class);

    return stockpriceDto.getData().stream()
        .map(e -> new LinePoint(e.getMarketDateTime().getYear(),
            e.getMarketDateTime().getMonthValue(),
            e.getMarketDateTime().getDayOfMonth(),
            e.getMarketDateTime().atZone(ZoneId.of("Asia/Hong_Kong")).getHour(), e.getMarketDateTime().getMinute(),
            e.getRegularMarketPrice()))
        .collect(Collectors.toList());

  }
}
