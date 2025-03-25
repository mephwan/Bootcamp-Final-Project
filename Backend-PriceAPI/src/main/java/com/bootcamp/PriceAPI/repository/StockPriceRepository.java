package com.bootcamp.PriceAPI.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.PriceAPI.entity.StockPriceEntity;

public interface StockPriceRepository extends JpaRepository<StockPriceEntity, Long> {
  boolean existsBySymbolAndRegularMarketTime(String symbol, Long regularMarketTime);

  Optional<StockPriceEntity> findFirstBySymbolOrderByMarketDateTimeDesc(String symbol);

  List<StockPriceEntity> findBySymbol(String symbol);
}
