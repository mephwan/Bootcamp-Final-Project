package com.bootcamp.yahoofinance.service.impl;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.entity.StockPriceEntity;
import com.bootcamp.yahoofinance.entity.StockPriceOHLCEntity;
import com.bootcamp.yahoofinance.DTO.StockOHLCDTO;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.repository.StockPriceOHLCRepository;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.bootcamp.yahoofinance.service.YahooService;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class YahooServiceImpl implements YahooService {
        @Autowired
        private StockPriceRepository stockPriceRepository;

        @Autowired
        private StockPriceOHLCRepository stockPriceOHLCRepository;

        @Autowired
        private RedisManager redisManager;

        @Override
        public StockDTO getPrice(String stockCode)
                        throws JsonProcessingException {

                // String redisString = "5min" + stockCode;

                // StockDTO redisData = this.redisManager.get(redisString,
                //                 StockDTO.class);

                // if (redisData != null) {
                //         StockDTO stockDTO = StockDTO.builder()
                //                         .symbol(redisData.getSymbol())
                //                         .timeFrame("M5")
                //                         .data(redisData.getData().stream()
                //                                         .filter(e -> e.getSymbol()
                //                                                         .equals(stockCode))
                //                                         .collect(Collectors
                //                                                         .toList()))
                //                         .build();

                //         System.out.println("Get From Redis.....");

                //         return stockDTO;
                // }

                LocalDate lastTradeDate = this.stockPriceRepository
                                .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                stockCode)
                                .orElseThrow(() -> new BusinessException())
                                .getMarketDateTime().toLocalDate();

                return StockDTO.builder().symbol(stockCode).timeFrame("M5")
                                .data(this.stockPriceRepository
                                                .findBySymbol(stockCode)
                                                .stream()
                                                .filter(e -> e.getMarketDateTime()
                                                                .toLocalDate()
                                                                .equals(lastTradeDate))
                                                .map(e -> StockData.builder()
                                                                .symbol(stockCode)
                                                                .regularMarketTime(
                                                                                e.getRegularMarketTime())
                                                                .marketDateTime(e
                                                                                .getMarketDateTime())
                                                                .regularMarketPrice(
                                                                                e.getRegularMarketPrice())
                                                                .regularMarketChangePercent(
                                                                                e.getRegularMarketChangePercent())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();

        }

        @Override
        public StockOHLCDTO getDayOHLC(String stockCode) {
                Long fromTimeStamp = LocalDate.now().plusYears(-1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> stockPriceOHLCEntities =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode);

                List<StockOHLCDTO.StockData> stockDataList =
                                stockPriceOHLCEntities.stream() //
                                                .filter(e -> e.getTimestamp() >= fromTimeStamp) //
                                                .map(e -> StockOHLCDTO.StockData
                                                                .builder() //
                                                                .symbol(e.getSymbol()) //
                                                                .timestamp(e.getTimestamp()) //
                                                                .marketDate(e.getDate()) //
                                                                .open(e.getOpen()) //
                                                                .high(e.getHigh()) //
                                                                .low(e.getLow()) //
                                                                .close(e.getClose()) //
                                                                .build() //
                                                ).collect(Collectors.toList());

                if (!stockDataList.get(stockDataList.size() - 1).getMarketDate()
                                .equals(LocalDate.now())) {

                        Double open = stockPriceRepository.findAll().stream()
                                        .filter(e -> e.getMarketDateTime()
                                                        .toLocalDate()
                                                        .equals(LocalDate.now())
                                                        & e.getSymbol().equals(
                                                                        stockCode)) //
                                        .map(e -> e.getRegularMarketPrice())
                                        .collect(Collectors.toList()).get(0);

                        Double close = stockPriceRepository
                                        .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                        stockCode)
                                        .map(e -> e.getRegularMarketPrice())
                                        .orElseThrow();

                        Long timeStamp = stockPriceRepository
                                        .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                        stockCode)
                                        .map(e -> e.getRegularMarketTime())
                                        .orElseThrow();

                        Double high = stockPriceRepository.findAll().stream()
                                        .filter(e -> e.getMarketDateTime()
                                                        .toLocalDate()
                                                        .equals(LocalDate.now())
                                                        & e.getSymbol().equals(
                                                                        stockCode)) //
                                        .map(e -> e.getRegularMarketPrice())
                                        .collect(Collectors.toList()).stream()
                                        .max((a, b) -> a.compareTo(b))
                                        .orElseThrow();

                        Double low = stockPriceRepository.findAll().stream()
                                        .filter(e -> e.getMarketDateTime()
                                                        .toLocalDate()
                                                        .equals(LocalDate.now())
                                                        & e.getSymbol().equals(
                                                                        stockCode)) //
                                        .map(e -> e.getRegularMarketPrice())
                                        .collect(Collectors.toList()).stream()
                                        .max((a, b) -> b.compareTo(a))
                                        .orElseThrow();

                        stockDataList.add(StockOHLCDTO.StockData.builder() //
                                        .symbol(stockCode) //
                                        .timestamp(timeStamp) //
                                        .marketDate(LocalDate.ofInstant(Instant
                                                        .ofEpochSecond(timeStamp),
                                                        ZoneId.systemDefault()))
                                        .open(open) //
                                        .high(high) //
                                        .low(low) //
                                        .close(close) //
                                        .build());
                }

                return StockOHLCDTO.builder().symbol(stockCode) //
                                .timeFrame("D1").data(stockDataList).build();
        }


        @Override
        public StockOHLCDTO getWeekOHLC(String stockCode) {

                Long fromTimeStamp = LocalDate.now().plusYears(-1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> stockPriceOHLCEntities =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode);

                List<StockOHLCDTO.StockData> stockDataList =
                                stockPriceOHLCEntities.stream() //
                                                .filter(e -> e.getTimestamp() >= fromTimeStamp
                                                                & LocalDate.ofInstant(
                                                                                Instant.ofEpochSecond(
                                                                                                e.getTimestamp()),
                                                                                ZoneId.systemDefault())
                                                                                .getDayOfWeek() == DayOfWeek.FRIDAY) //
                                                .map(e -> StockOHLCDTO.StockData
                                                                .builder() //
                                                                .symbol(e.getSymbol()) //
                                                                .timestamp(e.getTimestamp()) //
                                                                .marketDate(e.getDate()) //
                                                                .open(e.getOpen()) //
                                                                .high(e.getHigh()) //
                                                                .low(e.getLow()) //
                                                                .close(e.getClose()) //
                                                                .build() //
                                                ).collect(Collectors.toList());

                if (!stockDataList.get(stockDataList.size() - 1).getMarketDate()
                                .equals(LocalDate.now())) {

                        LocalDate mondayLocalDate = LocalDate.now()
                        .with(TemporalAdjusters.previous(
                                        DayOfWeek.MONDAY));
                        Long mondayTimeStamp = mondayLocalDate.atStartOfDay(ZoneId.systemDefault())
                        .toInstant().getEpochSecond();

                        List<StockPriceOHLCEntity> thisweek = this.stockPriceOHLCRepository.findBySymbol(stockCode).stream().filter(e -> e.getTimestamp() >= mondayTimeStamp).collect(Collectors.toList());

                        Double open = thisweek.get(0).getOpen();
                        Double high = thisweek.stream().map(e -> e.getHigh()).max((a, b) -> a.compareTo(b)).get();
                        Double low = thisweek.stream().map(e -> e.getLow()).min((a, b) -> b.compareTo(a)).get();
                        Double close = thisweek.get(thisweek.size()-1).getClose();

                        stockDataList.add(StockOHLCDTO.StockData
                        .builder() //
                        .symbol(stockCode) //
                        .timestamp(thisweek.get(thisweek.size()-1).getTimestamp()) //
                        .marketDate(thisweek.get(thisweek.size()-1).getDate()) //
                        .open(open) //
                        .high(high) //
                        .low(low) //
                        .close(close) //
                        .build());
                }

                return StockOHLCDTO.builder().symbol(stockCode) //
                                .timeFrame("W1").data(stockDataList).build();
        }

        @Override
        public StockOHLCDTO getMonthOHLC(String stockCode) {

                Long fromTimeStamp = LocalDate.now().plusYears(-2)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> stockPriceOHLCEntities =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode);

                List<StockOHLCDTO.StockData> stockDataList =
                                stockPriceOHLCEntities.stream() //
                                                .filter(e -> e.getTimestamp() >= fromTimeStamp
                                                                & LocalDate.ofInstant(
                                                                                Instant.ofEpochSecond(
                                                                                                e.getTimestamp()),
                                                                                ZoneId.systemDefault())
                                                                                .getDayOfMonth() == LocalDate
                                                                                                .ofInstant(Instant
                                                                                                                .ofEpochSecond(e.getTimestamp()),
                                                                                                                ZoneId.systemDefault())
                                                                                                .lengthOfMonth()) //
                                                .map(e -> StockOHLCDTO.StockData
                                                                .builder() //
                                                                .symbol(e.getSymbol()) //
                                                                .timestamp(e.getTimestamp()) //
                                                                .marketDate(e.getDate()) //
                                                                .open(e.getOpen()) //
                                                                .high(e.getHigh()) //
                                                                .low(e.getLow()) //
                                                                .close(e.getClose()) //
                                                                .build() //
                                                ).collect(Collectors.toList());

                return StockOHLCDTO.builder().symbol(stockCode) //
                                .timeFrame("MN").data(stockDataList).build();
        }
}
