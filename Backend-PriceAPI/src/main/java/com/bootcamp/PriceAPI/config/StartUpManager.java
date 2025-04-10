package com.bootcamp.PriceAPI.config;

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
import com.bootcamp.PriceAPI.entity.StockListEntity;
import com.bootcamp.PriceAPI.entity.StockPriceEntity;
import com.bootcamp.PriceAPI.entity.StockPriceOHLCEntity;
import com.bootcamp.PriceAPI.lib.YahooManager;
import com.bootcamp.PriceAPI.model.YahooStockPriceDto;
import com.bootcamp.PriceAPI.model.YahooOHLCDto;
import com.bootcamp.PriceAPI.repository.StockListRepository;
import com.bootcamp.PriceAPI.repository.StockPriceOHLCRepository;
import com.bootcamp.PriceAPI.repository.StockPriceRepository;
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
  private YahooManager yahooManager;

  @Autowired
  private RestTemplate restTemplate;

  @Override
  public void run(String... args) throws JsonProcessingException {

    List<String> stockCodeList = addStockListEntity();

    getStockPriceFromYahoo(stockCodeList);

    for (String stockCode : stockCodeList) {
      getOHLC(stockCode, 2);

    }

  }


  private List<String> addStockListEntity() {
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

    return stockList.stream().map(e -> e.getSymbol())
        .collect(Collectors.toList());
  }

  private void getStockPriceFromYahoo(List<String> stockCodeList) {
    stockCodeList = this.stockListRepository.findAll().stream()
        .map(e -> e.getSymbol()).collect(Collectors.toList());

    for (String stockCode : stockCodeList) {
      Optional<StockPriceEntity> stockOptional = this.stockPriceRepository
          .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode);

      if (!stockOptional.isPresent()) {


        YahooStockPriceDto yahooDto = null;

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


  private void getOHLC(String stockCode, int year) {

    Long fromTimeStamp = LocalDate.now().plusYears(-1 * year)
        .atStartOfDay(ZoneId.systemDefault()).toInstant().getEpochSecond();
    Long toTimeStamp = LocalDate.now().atStartOfDay(ZoneId.systemDefault())
        .toInstant().getEpochSecond();

    YahooOHLCDto yahooOHLCDto = null;

    while (yahooOHLCDto == null) {

      System.out.println("Getting OHLC for " + stockCode + ".........");
      yahooOHLCDto = this.yahooManager.getOhlcDto(this.restTemplate, stockCode,
          fromTimeStamp, toTimeStamp);

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

        LocalDate marketDate =
            LocalDateTime.ofInstant(Instant.ofEpochSecond(timestemp.get(i)),
                ZoneId.systemDefault()).toLocalDate();

        this.stockPriceOHLCRepository
            .save(StockPriceOHLCEntity.builder().symbol(stockCode) //
                .open(open.get(i)) //
                .high(high.get(i)) //
                .low(low.get(i)) //
                .close(close.get(i)) //
                .timestamp(timestemp.get(i)) //
                .date(marketDate).build());

      }
    }
  }
}
