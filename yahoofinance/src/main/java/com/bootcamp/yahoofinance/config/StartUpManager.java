package com.bootcamp.yahoofinance.config;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.entity.StockListEntity;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.repository.StockListRepository;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class StartUpManager implements CommandLineRunner {
  @Autowired
  private StockListRepository stockListRepository;

  @Autowired
  private StockPriceRepository stockPriceRepository;

  @Autowired
  private RedisManager redisManager;

  @Override
  public void run(String... args) throws JsonProcessingException {

    List<StockListEntity> stockList = new ArrayList<>();
    List<String> stockCodeList = new ArrayList<>();

    stockCodeList.add("0388.HK");
    stockCodeList.add("0700.HK");
    stockCodeList.add("0005.HK");

    for (String stockCode : stockCodeList) {
      stockList.add(new StockListEntity(stockCode));
    }

    this.stockListRepository.deleteAll();
    this.stockListRepository.saveAll(stockList);

    for (String stockCode : stockCodeList) {
      LocalDate lastTradeDate = this.stockPriceRepository
          .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode)
          .orElseThrow(() -> new BusinessException()).getMarketDateTime()
          .toLocalDate();

      StockDTO stockDTO =
          StockDTO.builder().symbol(stockCode).timeFrame("M5")
              .data(this.stockPriceRepository.findBySymbol(stockCode).stream()
                  .filter(e -> e.getMarketDateTime().toLocalDate()
                      .equals(lastTradeDate))
                  .map(e -> StockData.builder().symbol(e.getSymbol())
                      .regularMarketTime(e.getRegularMarketTime())
                      .marketDateTime(e.getMarketDateTime())
                      .regularMarketPrice(e.getRegularMarketPrice())
                      .regularMarketChangePercent(
                          e.getRegularMarketChangePercent())
                      .build())
                  .collect(Collectors.toList()))
              .build();

      String redisString = "5min" + stockCode;

      this.redisManager.set(redisString, stockDTO, Duration.ofMinutes(1));
    }

  }
}
