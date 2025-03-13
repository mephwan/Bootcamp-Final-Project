package com.bootcamp.yahoofinance.DTO;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@Builder
@EqualsAndHashCode
public class StockDTO {
  private String symbol;
  private LocalDateTime marketDateTime;
  private Double regularMarketPrice;

  private String type;
}
