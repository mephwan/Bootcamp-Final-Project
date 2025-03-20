package com.bootcamp.yahoofinance.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.yahoofinance.entity.StockPriceOHLCEntity;

public interface StockPriceOHLCRepository extends JpaRepository<StockPriceOHLCEntity, Long> {
  boolean existsBySymbolAndTimestamp(String symbol, Long timestamp);

  List<StockPriceOHLCEntity> findBySymbol(String symbol);
}
