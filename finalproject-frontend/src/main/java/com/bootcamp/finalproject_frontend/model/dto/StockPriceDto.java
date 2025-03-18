package com.bootcamp.finalproject_frontend.model.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDto {
    private String symbol;
  private String timeFrame;
  private List<StockData> data;

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class StockData {
    private String symbol;
    private Long regularMarketTime;
    private LocalDateTime marketDateTime;
    private Double regularMarketPrice;
    private Double regularMarketChangePercent;
  }
}
