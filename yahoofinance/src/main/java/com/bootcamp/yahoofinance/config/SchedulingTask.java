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
import com.bootcamp.yahoofinance.entity.StockPriceOHLCEntity;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.bootcamp.yahoofinance.repository.StockListRepository;
import com.bootcamp.yahoofinance.repository.StockPriceOHLCRepository;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;

@Component
public class SchedulingTask {
  @Autowired
  private StockListRepository stockListRepository;

  @Autowired
  private StockPriceRepository stockPriceRepository;

  @Autowired
  private StockPriceOHLCRepository stockPriceOHLCRepository;

  @Autowired
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RedisManager redisManager;

  @Scheduled(cron = "10 */5 * * * *")
  public void getYahooFiveMintus() throws Exception {

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
        .map(e -> {
          ZoneId zone = ZoneId.systemDefault();
          if (e.getSymbol().contains("HK")) {
            zone = ZoneId.systemDefault();
          } else {
            zone = ZoneId.of("America/New_York");
          }
          return StockPriceEntity.builder() //
            .symbol(e.getSymbol()) //
            .regularMarketTime(e.getRegularMarketTime()) //
            .regularMarketPrice(e.getRegularMarketPrice()) //
            .regularMarketChangePercent(e.getRegularMarketChangePercent()) //
            .type("M5") //
            .marketDateTime(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(e.getRegularMarketTime()),
                zone)) //
            .build();
          }) //
        .collect(Collectors.toList());

    stockPriceEntities
        .forEach(e -> e.setApiDatetime(System.currentTimeMillis() / 1000));

    stockPriceRepository.saveAll(stockPriceEntities);

  }

  @Scheduled(cron = "10 0 8 * * *")
  public void getOHLCAtEight() {

    List<String> stockCodeList = this.stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    Long fromTimeStamp = LocalDate.now().plusDays(-2).atStartOfDay(ZoneId.systemDefault())
        .toInstant().getEpochSecond();
    Long toTimeStamp = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        .toInstant().getEpochSecond();

    for (String stockCode : stockCodeList) {
      ZoneId zone = ZoneId.systemDefault();
      if (!stockCode.contains("HK")) {
        zone = ZoneId.of("America/New_York");
      }
      YahooOHLCDto yahooOHLCDto = null;

      while (yahooOHLCDto == null) {

        System.out.println("Getting OHLC for " + stockCode + ".........");
        yahooOHLCDto = this.yahooManager.getOhlcDto(this.restTemplate,
            stockCode, fromTimeStamp, toTimeStamp);

        try {
          TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
          System.out.println("Retry......");
        }

      }

      List<Long> timestemp =
          yahooOHLCDto.getChart().getResult().get(0).getTimestamp();
      List<Double> high = yahooOHLCDto.getChart().getResult().get(0)
          .getIndicators().getQuote().get(0).getHigh();
      List<Double> open = yahooOHLCDto.getChart().getResult().get(0)
          .getIndicators().getQuote().get(0).getOpen();
      List<Double> low = yahooOHLCDto.getChart().getResult().get(0)
          .getIndicators().getQuote().get(0).getLow();
      List<Double> close = yahooOHLCDto.getChart().getResult().get(0)
          .getIndicators().getQuote().get(0).getClose();

      for (int i = 0; i < timestemp.size(); i++) {

        if (!this.stockPriceOHLCRepository.existsBySymbolAndTimestamp(stockCode,
            timestemp.get(i)) && close.get(i) != null) {

          this.stockPriceOHLCRepository
              .save(StockPriceOHLCEntity.builder().symbol(stockCode) //
                  .open(open.get(i)) //
                  .high(high.get(i)) //
                  .low(low.get(i)) //
                  .close(close.get(i)) //
                  .timestamp(timestemp.get(i)) //
                  .date(Instant.ofEpochSecond(timestemp.get(i)).atZone(zone).toLocalDate())
                  .build());
        }
      }
    }
  }
}
