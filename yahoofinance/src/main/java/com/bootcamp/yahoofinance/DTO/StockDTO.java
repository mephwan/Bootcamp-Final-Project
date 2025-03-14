package com.bootcamp.yahoofinance.DTO;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class StockDTO {

  private String symbol;
  private String timeFrame;
  private List<StockData> data;

  @Getter
  @Setter
  @Builder
  public static class StockData {
    private Long regularMarketTime;
    private LocalDateTime marketDateTime;
    private Double regularMarketPrice;
    private Double regularMarketChangePercent;
  }
}

