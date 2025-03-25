package com.bootcamp.StockBoard.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.bootcamp.StockBoard.dto.LinePointDTO;

public interface LineChartOperation {
  @GetMapping(value = "/chart/line")
  List<LinePointDTO> getLineChart(@RequestParam String interval, @RequestParam String symbol);
}
