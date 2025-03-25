package com.bootcamp.StockBoard.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LinePointDTO {
  private Long dateTime;
  private Double close;
}
