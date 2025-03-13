package com.bootcamp.yahoofinance.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_price")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockPriceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String symbol;
  private Long regularMarketTime;
  private Double regularMarketPrice;
  private Double regularMarketChangePercent;
  private Double bid;
  private Double ask;

  private String type;
  private Long apiDatetime;
  private LocalDateTime marketDateTime;

  private String marketState;
}
