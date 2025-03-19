package com.bootcamp.yahoofinance.config;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedList;
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
import com.bootcamp.yahoofinance.entity.StockPriceOHLCEntity;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooDto;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.bootcamp.yahoofinance.repository.StockListRepository;
import com.bootcamp.yahoofinance.repository.StockPriceOHLCRepository;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
public class StartUpManager implements CommandLineRunner {
  @Autowired
  private StockListRepository stockListRepository;

  @Autowired
  private StockPriceRepository stockPriceRepository;

  @Autowired
  private StockPriceOHLCRepository stockPriceOHLCRepository;

  @Autowired
  private RedisManager redisManager;

  @Autowired
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public void run(String... args) throws JsonProcessingException {

    addStockListEntity();

    getStockPriceFromYahoo();

    getOHLC();

    setToRedis();

  }


  private void addStockListEntity() {
    List<StockListEntity> stockList = new ArrayList<>();

    stockList.add(new StockListEntity("0700.HK", "HK"));
    stockList.add(new StockListEntity("9988.HK", "HK"));
    stockList.add(new StockListEntity("0941.HK", "HK"));
    stockList.add(new StockListEntity("0939.HK", "HK"));
    stockList.add(new StockListEntity("0005.HK", "HK"));
    stockList.add(new StockListEntity("1810.HK", "HK"));
    stockList.add(new StockListEntity("3690.HK", "HK"));
    stockList.add(new StockListEntity("0883.HK", "HK"));
    stockList.add(new StockListEntity("1299.HK", "HK"));
    stockList.add(new StockListEntity("9618.HK", "HK"));

    stockList.add(new StockListEntity("AAPL", "US"));
    stockList.add(new StockListEntity("MSFT", "US"));
    stockList.add(new StockListEntity("NVDA", "US"));
    stockList.add(new StockListEntity("AMZN", "US"));
    stockList.add(new StockListEntity("WMT", "US"));
    stockList.add(new StockListEntity("JPM", "US"));
    stockList.add(new StockListEntity("V", "US"));
    stockList.add(new StockListEntity("UNH", "US"));
    stockList.add(new StockListEntity("JNJ", "US"));
    stockList.add(new StockListEntity("PG", "US"));

    stockList.add(new StockListEntity("BTC-USD", "Crypto"));
    stockList.add(new StockListEntity("ETH-USD", "Crypto"));

    this.stockListRepository.deleteAll();
    this.stockListRepository.saveAll(stockList);
  }

  private void getStockPriceFromYahoo() {
    List<String> stockCodeList = this.stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    for (String stockCode : stockCodeList) {
      Optional<StockPriceEntity> stockOptional = this.stockPriceRepository
          .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode);

      if (!stockOptional.isPresent()) {


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
    }
  }

  private void setToRedis() throws JsonProcessingException {
    LocalDate lastTradeDate = null;
    List<String> stockCodeList = this.stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    for (String stockCode : stockCodeList) {
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

  private void getOHLC() {
    YahooOHLCDto yahooOHLCDto = this.yahooManager.getOhlcDto(this.restTemplate);
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

    List<StockPriceOHLCEntity> stockPriceOHLCEntities = new LinkedList<>();

    for (int i = 0; i < timestemp.size(); i++) {
      stockPriceOHLCEntities.add(StockPriceOHLCEntity.builder()
          .symbol("0388.HK") //
          .open(open.get(i)) //
          .high(high.get(i)) //
          .low(low.get(i)) //
          .close(close.get(i)) //
          .timestamp(timestemp.get(i)) //
          .date(LocalDateTime.ofInstant(Instant.ofEpochSecond(timestemp.get(i)), ZoneId.systemDefault()).toLocalDate())
          .build());
    }

    this.stockPriceOHLCRepository.saveAll(stockPriceOHLCEntities);

  }
}
