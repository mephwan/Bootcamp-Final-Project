package com.bootcamp.yahoofinance.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.bootcamp.yahoofinance.entity.StockListEntity;
import com.bootcamp.yahoofinance.repository.StockListRepository;

@Component
public class StartUpManager implements CommandLineRunner {
  @Autowired
  private StockListRepository stockListRepository;
  
  @Override
  public void run(String... args) throws Exception {

    List<StockListEntity> stockList = new ArrayList<>();

    stockList.add(new StockListEntity("0388.HK"));
    stockList.add(new StockListEntity("0700.HK"));
    stockList.add(new StockListEntity("0005.HK"));

    stockListRepository.deleteAll();
    stockListRepository.saveAll(stockList);

  }
}
