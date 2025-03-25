package com.bootcamp.StockBoard.model.dto;

import java.time.LocalDate;
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
public class StockOHLCDto {
  private Long code;
  private String message;
  private StockPriceBody body;

  @Getter
  @Setter
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class StockPriceBody {
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
      private Long timestamp;
      private LocalDate marketDate;
      private Double open;
      private Double high;
      private Double low;
      private Double close;
    }
  }
}
