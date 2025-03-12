package com.bootcamp.yahoofinance.config;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
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

  @Scheduled(cron = "0 */5 09-17 * * MON-FRI")
  public void getYahoo() {
    LocalTime now = LocalTime.now();

    if (now.isBefore(LocalTime.of(9, 30)) || now.isAfter(LocalTime.of(16, 30))) {
      return;
    }

    List<String> stockList = stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    YahooDto yahooDtos =
        yahooManager.getYahooDtos(this.restTemplate, stockList);

    List<StockPriceEntity> stockPriceEntities = yahooDtos.getQuoteResponse()
        .getResult().stream()
        .map(e -> StockPriceEntity.builder().symbol(e.getSymbol())
            .regularMarketTime(e.getRegularMarketTime())
            .regularMarketPrice(e.getRegularMarketPrice())
            .regularMarketChangePercent(e.getRegularMarketChangePercent())
            .bid(e.getBid()).ask(e.getAsk()).type("M5")
            .apiDatetime(System.currentTimeMillis() / 1000)
            .marketDateTime(LocalDateTime.ofInstant(
                Instant.ofEpochSecond(e.getRegularMarketTime()),
                ZoneId.systemDefault()))
            .build())
        .collect(Collectors.toList());

    stockPriceRepository.saveAll(stockPriceEntities);
  }
}
