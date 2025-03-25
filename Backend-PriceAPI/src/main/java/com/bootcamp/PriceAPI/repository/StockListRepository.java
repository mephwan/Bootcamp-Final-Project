package com.bootcamp.PriceAPI.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bootcamp.PriceAPI.entity.StockListEntity;

public interface StockListRepository extends JpaRepository<StockListEntity, Long> {
  
}
