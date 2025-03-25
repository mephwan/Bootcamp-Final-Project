package com.bootcamp.PriceAPI.entity;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_price_OHLC")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceOHLCEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private String symbol;

  private Double open;
  private Double high;
  private Double low;
  private Double close;

  private Long timestamp;

  private LocalDate date; 
}
