package com.bootcamp.StockBoard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CandleStickDTO {
  private Long date;
  private Double open;
  private Double high;
  private Double low;
  private Double close;
}
