package com.bootcamp.yahoofinance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.yahoofinance.entity.StockPriceEntity;

public interface StockPriceRepository extends JpaRepository<StockPriceEntity, Long> {
  
}
