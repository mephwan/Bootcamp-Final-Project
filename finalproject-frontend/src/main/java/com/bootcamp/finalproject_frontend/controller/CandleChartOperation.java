package com.bootcamp.finalproject_frontend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.finalproject_frontend.dto.CandleStickDTO;

public interface CandleChartOperation {
  
  @GetMapping(value = "/chart/candle")
  List<CandleStickDTO> getCandleChart(@RequestParam String interval, @RequestParam String symbol);
}
