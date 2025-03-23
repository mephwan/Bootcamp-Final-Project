package com.bootcamp.yahoofinance.DTO;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockOHLCDTO {
  
  private String symbol;
  private String timeFrame;
  private List<StockData> data;

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  public static class StockData {
    private String symbol;
    private Long timestamp;
    private LocalDate marketDate;
    private Double open;
    private Double high;
    private Double low;
    private Double close;
  }
}
