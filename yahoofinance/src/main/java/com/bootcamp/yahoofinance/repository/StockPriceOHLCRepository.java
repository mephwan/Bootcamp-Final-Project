package com.bootcamp.yahoofinance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.yahoofinance.entity.StockPriceOHLCEntity;

public interface StockPriceOHLCRepository extends JpaRepository<StockPriceOHLCEntity, Long> {
  
}
