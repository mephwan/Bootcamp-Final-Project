package com.bootcamp.yahoofinance.config;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.entity.StockPriceEntity;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.repository.StockListRepository;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;

@Component
public class SchedulingTask {
  @Autowired
  private StockListRepository stockListRepository;

  @Autowired
  private StockPriceRepository stockPriceRepository;

  @Autowired
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RedisManager redisManager;

  @Scheduled(cron = "10 */5 * * * MON-FRI")
  public void getYahoo() throws Exception {

    List<String> stockCodeList = stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    YahooDto yahooDto = null;

    while (yahooDto == null) {
      try {
        yahooDto = yahooManager.getYahooDtos(this.restTemplate, stockCodeList);
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

    List<StockPriceEntity> stockPriceEntities = yahooDto.getQuoteResponse()
        .getResult().stream() //
        .filter(e -> !stockPriceRepository.existsBySymbolAndRegularMarketTime(
            e.getSymbol(), e.getRegularMarketTime())) //
        .map(e -> StockPriceEntity.builder() //
            .symbol(e.getSymbol()) //
            .regularMarketTime(e.getRegularMarketTime()) //
            .regularMarketPrice(e.getRegularMarketPrice()) //
            .regularMarketChangePercent(e.getRegularMarketChangePercent()) //
            .type("M5") //
            .marketDateTime(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(e.getRegularMarketTime()),
                ZoneId.systemDefault())) //
            .build()) //
        .collect(Collectors.toList());

    stockPriceEntities
        .forEach(e -> e.setApiDatetime(System.currentTimeMillis() / 1000));

    stockPriceRepository.saveAll(stockPriceEntities);

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
                  .map(e -> StockData.builder()
                  .symbol(e.getSymbol())
                      .regularMarketTime(e.getRegularMarketTime())
                      .marketDateTime(e.getMarketDateTime())
                      .regularMarketPrice(e.getRegularMarketPrice())
                      .regularMarketChangePercent(
                          e.getRegularMarketChangePercent())
                      .build())
                  .collect(Collectors.toList()))
              .build();

      String redisString = "5min" + stockCode;

      this.redisManager.set(redisString, stockDTO, Duration.ofMinutes(5));
    }
  }
}
