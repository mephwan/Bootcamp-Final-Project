package com.bootcamp.yahoofinance.config;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.entity.StockListEntity;
import com.bootcamp.yahoofinance.entity.StockPriceEntity;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooDto;
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

  @Autowired
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

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

    LocalDate lastTradeDate = null;

    for (String stockCode : stockCodeList) {
      Optional<StockPriceEntity> stockOptional = this.stockPriceRepository
          .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode);

      if (stockOptional.isPresent()) {
        lastTradeDate = stockOptional.get().getMarketDateTime().toLocalDate();
      } else {

        YahooDto yahooDto = null;

        while (yahooDto == null) {
          try {
            yahooDto =
                yahooManager.getYahooDtos(this.restTemplate, stockCodeList);
          } catch (HttpClientErrorException e) {
            try {
              TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e2) {
              System.out.println("Retry....");
            }
          } catch (ResourceAccessException e3) {
            System.out.println("Retry....");
          }
        }

        lastTradeDate = yahooDto.getQuoteResponse().getResult().stream()
            .filter(e -> e.getSymbol().equals(stockCode)).findAny()
            .map(e -> LocalDateTime.ofInstant(
                Instant.ofEpochSecond(e.getRegularMarketTime()),
                ZoneId.systemDefault()))
            .orElseThrow(() -> new BusinessException()).toLocalDate();

        List<StockPriceEntity> stockPriceEntities = yahooDto.getQuoteResponse()
            .getResult().stream() //
            .filter(
                e -> !stockPriceRepository.existsBySymbolAndRegularMarketTime(
                    e.getSymbol(), e.getRegularMarketTime())) //
            .map(e -> StockPriceEntity.builder() //
                .symbol(e.getSymbol()) //
                .regularMarketTime(e.getRegularMarketTime()) //
                .regularMarketPrice(e.getRegularMarketPrice()) //
                .regularMarketChangePercent(e.getRegularMarketChangePercent()) //
                .bid(e.getBid()) //
                .ask(e.getAsk()) //
                .type("M5") //
                .marketDateTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(e.getRegularMarketTime()),
                    ZoneId.systemDefault())) //
                .build()) //
            .collect(Collectors.toList());

        stockPriceEntities
            .forEach(e -> e.setApiDatetime(System.currentTimeMillis() / 1000));

        stockPriceRepository.saveAll(stockPriceEntities);
      }

      LocalDate finalLastTradeDate = lastTradeDate;

      StockDTO stockDTO =
          StockDTO.builder().symbol(stockCode).timeFrame("M5")
              .data(this.stockPriceRepository.findBySymbol(stockCode).stream()
                  .filter(e -> e.getMarketDateTime().toLocalDate()
                      .equals(finalLastTradeDate))
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

      this.redisManager.set(redisString, stockDTO, Duration.ofHours(12));
    }

  }
}
