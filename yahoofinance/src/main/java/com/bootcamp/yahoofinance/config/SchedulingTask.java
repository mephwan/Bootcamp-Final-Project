package com.bootcamp.yahoofinance.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.bootcamp.yahoofinance.entity.StockPriceEntity;
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

  @Scheduled(cron = "10 */5 09-17 * * MON-FRI")
  public void getYahoo() {
    LocalTime now = LocalTime.now();

    if (now.isBefore(LocalTime.of(9, 30))
        || now.isAfter(LocalTime.of(16, 30))) {
      return;
    }

    List<String> stockList = stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    YahooDto yahooDto = null;

    while (yahooDto == null) {
      try {
        yahooDto = yahooManager.getYahooDtos(this.restTemplate, stockList);
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

    List<StockPriceEntity> stockPriceEntities =
        yahooDto.getQuoteResponse().getResult().stream()
            .map(e -> StockPriceEntity.builder().symbol(e.getSymbol())
                .regularMarketTime(e.getRegularMarketTime())
                .regularMarketPrice(e.getRegularMarketPrice())
                .regularMarketChangePercent(e.getRegularMarketChangePercent())
                .bid(e.getBid()).ask(e.getAsk()).type("M5")
                .marketDateTime(LocalDateTime.ofInstant(
                    Instant.ofEpochSecond(e.getRegularMarketTime()),
                    ZoneId.systemDefault()))
                .marketState(e.getMarketState()).build())
            .distinct().collect(Collectors.toList());

    // System.currentTimeMillis() / 1000)

    stockPriceEntities.forEach(e -> e.setApiDatetime(System.currentTimeMillis() / 1000));
      
    stockPriceRepository.saveAll(stockPriceEntities);
  }
}
