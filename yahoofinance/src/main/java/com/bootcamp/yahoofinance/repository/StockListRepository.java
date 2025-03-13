package com.bootcamp.yahoofinance.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.yahoofinance.entity.StockListEntity;

public interface StockListRepository extends JpaRepository<StockListEntity, Long> {
  
}
