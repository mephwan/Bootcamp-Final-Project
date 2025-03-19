package com.bootcamp.yahoofinance.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
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

  @Column(name = "regular_market_time")
  private Long regularMarketTime;

  @Column(name = "regular_market_price")
  private Double regularMarketPrice;

  @Column(name = "regular_market_change_percent")
  private Double regularMarketChangePercent;

  private String type;
  private Long apiDatetime;

  @Column(name = "market_date_time")
  private LocalDateTime marketDateTime;
}
