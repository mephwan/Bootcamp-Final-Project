package com.bootcamp.yahoofinance.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock_list")
@Getter
@NoArgsConstructor
public class StockListEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String symbol;
  private String market;

  public StockListEntity(String symbol, String market) {
    this.symbol = symbol;
    this.market = market;
  }
}
