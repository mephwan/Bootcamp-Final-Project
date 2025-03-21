package com.bootcamp.yahoofinance.service.impl;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
                // StockDTO.class);

                // if (redisData != null) {
                // StockDTO stockDTO = StockDTO.builder()
                // .symbol(redisData.getSymbol())
                // .timeFrame("M5")
                // .data(redisData.getData().stream()
                // .filter(e -> e.getSymbol()
                // .equals(stockCode))
                // .collect(Collectors
                // .toList()))
                // .build();

                // System.out.println("Get From Redis.....");

                // return stockDTO;
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

                LocalDate today = LocalDate.now();

                Long getFromTimeStamp = today.plusYears(-1).withDayOfMonth(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> dailyPrices =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode)
                                                .stream()
                                                .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                .collect(Collectors.toList());

                List<StockOHLCDTO.StockData> datas = dailyPrices.stream()
                                .map(e -> StockOHLCDTO.StockData.builder()
                                                .symbol(stockCode)
                                                .timestamp(e.getTimestamp()) //
                                                .marketDate(e.getDate()) //
                                                .open(e.getOpen()) //
                                                .high(e.getHigh()) //
                                                .low(e.getLow()) //
                                                .close(e.getClose()) //
                                                .build())
                                .collect(Collectors.toList());

                StockOHLCDTO.StockData lastUpdatePrice =
                                getLastUpdateOHLC(stockCode);

                if (lastUpdatePrice != null) {
                        datas.add(lastUpdatePrice);
                }

                List<StockOHLCDTO.StockData> dailyOHLC = new LinkedList<>();

                for (int i = 0; i < 360; i++) {

                        LocalDate date = today.plusDays(-1 * i);

                        Optional<StockOHLCDTO.StockData> oneDayOHLC = datas
                                        .stream() //
                                        .filter(e -> e.getMarketDate()
                                                        .equals(date)) //
                                        .findFirst();

                        if (oneDayOHLC.isPresent()) {


                                dailyOHLC.add(StockOHLCDTO.StockData.builder() //
                                                .symbol(stockCode) //
                                                .timestamp(oneDayOHLC.get().getTimestamp()) //
                                                .marketDate(oneDayOHLC.get().getMarketDate()) //
                                                .open(oneDayOHLC.get().getOpen()) //
                                                .high(oneDayOHLC.get().getHigh()) //
                                                .low(oneDayOHLC.get().getLow()) //
                                                .close(oneDayOHLC.get().getClose()) //
                                                .build());
                        }

                }

                return StockOHLCDTO.builder().symbol(stockCode).timeFrame("D1")
                                .data(dailyOHLC).build();
        }


        @Override
        public StockOHLCDTO getWeekOHLC(String stockCode) {

                LocalDate today = LocalDate.now();

                Long getFromTimeStamp = today.plusYears(-1).withDayOfMonth(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> dailyPrices =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode)
                                                .stream()
                                                .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                .collect(Collectors.toList());

                List<StockOHLCDTO.StockData> datas = dailyPrices.stream()
                                .map(e -> StockOHLCDTO.StockData.builder()
                                                .symbol(stockCode)
                                                .timestamp(e.getTimestamp()) //
                                                .marketDate(e.getDate()) //
                                                .open(e.getOpen()) //
                                                .high(e.getHigh()) //
                                                .low(e.getLow()) //
                                                .close(e.getClose()) //
                                                .build())
                                .collect(Collectors.toList());

                StockOHLCDTO.StockData lastUpdatePrice =
                                getLastUpdateOHLC(stockCode);

                if (lastUpdatePrice != null) {
                        datas.add(lastUpdatePrice);
                }

                List<StockOHLCDTO.StockData> weeklyOHLC = new LinkedList<>();

                for (int i = 0; i < 52; i++) {

                        LocalDate weekDate = today.plusWeeks(-1 * i);

                        List<StockOHLCDTO.StockData> oneWeekOHLC = datas
                                        .stream() //
                                        .filter(e -> getDatesInSameWeek(
                                                        e.getMarketDate(),
                                                        weekDate)) //
                                        .collect(Collectors.toList());

                        Double open = oneWeekOHLC.get(0).getOpen();

                        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAA" + oneWeekOHLC.get(0).getOpen());
                        Double high = oneWeekOHLC.stream().map(e -> e.getHigh())
                                        .max((a, b) -> a.compareTo(b)).get();
                        Double low = oneWeekOHLC.stream().map(e -> e.getLow())
                                        .min(Comparator.naturalOrder()).get();
                        Double close = oneWeekOHLC.get(oneWeekOHLC.size() - 1)
                                        .getClose();

                        weeklyOHLC.add(StockOHLCDTO.StockData.builder() //
                                        .symbol(stockCode) //
                                        .timestamp(oneWeekOHLC
                                                        .get(oneWeekOHLC.size()
                                                                        - 1)
                                                        .getTimestamp()) //
                                        .marketDate(oneWeekOHLC
                                                        .get(oneWeekOHLC.size()
                                                                        - 1)
                                                        .getMarketDate()) //
                                        .open(open) //
                                        .high(high) //
                                        .low(low) //
                                        .close(close) //
                                        .build());

                }

                return StockOHLCDTO.builder().symbol(stockCode).timeFrame("MN")
                                .data(weeklyOHLC).build();
        }

        @Override
        public StockOHLCDTO getMonthOHLC(String stockCode) {

                LocalDate today = LocalDate.now();
                Long getFromTimeStamp = today.plusYears(-2).withDayOfMonth(1)
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant().getEpochSecond();

                List<StockPriceOHLCEntity> dailyPrices =
                                this.stockPriceOHLCRepository
                                                .findBySymbol(stockCode)
                                                .stream()
                                                .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                .collect(Collectors.toList());

                List<StockOHLCDTO.StockData> datas = dailyPrices.stream()
                                .map(e -> StockOHLCDTO.StockData.builder()
                                                .symbol(stockCode)
                                                .timestamp(e.getTimestamp()) //
                                                .marketDate(e.getDate()) //
                                                .open(e.getOpen()) //
                                                .high(e.getHigh()) //
                                                .low(e.getLow()) //
                                                .close(e.getClose()) //
                                                .build())
                                .collect(Collectors.toList());

                StockOHLCDTO.StockData lastUpdatePrice =
                                getLastUpdateOHLC(stockCode);

                if (lastUpdatePrice != null) {
                        datas.add(lastUpdatePrice);
                }

                List<StockOHLCDTO.StockData> monthlyOHLC = new LinkedList<>();

                for (int i = 0; i < 24; i++) {

                        LocalDate month = today.plusMonths(-1 * i);
                        List<StockOHLCDTO.StockData> oneMonthOHLC = datas
                                        .stream() //
                                        .filter(e -> e.getMarketDate()
                                                        .getMonthValue() == month
                                                                        .getMonthValue()
                                                        & e.getMarketDate()
                                                                        .getYear() == month
                                                                                        .getYear()) //
                                        .collect(Collectors.toList());

                        Double open = oneMonthOHLC.get(0).getOpen();
                        Double high = oneMonthOHLC.stream()
                                        .map(e -> e.getHigh())
                                        .max((a, b) -> a.compareTo(b)).get();
                        Double low = oneMonthOHLC.stream().map(e -> e.getLow())
                                        .min(Comparator.naturalOrder()).get();
                        Double close = oneMonthOHLC.get(oneMonthOHLC.size() - 1)
                                        .getClose();

                        monthlyOHLC.add(StockOHLCDTO.StockData.builder() //
                                        .symbol(stockCode) //
                                        .timestamp(oneMonthOHLC
                                                        .get(oneMonthOHLC.size()
                                                                        - 1)
                                                        .getTimestamp()) //
                                        .marketDate(oneMonthOHLC
                                                        .get(oneMonthOHLC.size()
                                                                        - 1)
                                                        .getMarketDate()) //
                                        .open(open) //
                                        .high(high) //
                                        .low(low) //
                                        .close(close) //
                                        .build());

                }

                return StockOHLCDTO.builder().symbol(stockCode).timeFrame("MN")
                                .data(monthlyOHLC).build();

        }

        private StockOHLCDTO.StockData getLastUpdateOHLC(String stockCode) {
                LocalDate lastDate = this.stockPriceRepository
                                .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                stockCode)
                                .get().getMarketDateTime().toLocalDate();
                LocalDate lastOHLCDate = this.stockPriceOHLCRepository
                                .findFirstBySymbolOrderByTimestampDesc(
                                                stockCode)
                                .get().getDate();

                if (!lastDate.equals(lastOHLCDate)) {
                        List<StockPriceEntity> lastDatePrices =
                                        this.stockPriceRepository
                                                        .findBySymbol(stockCode)
                                                        .stream()
                                                        .filter(e -> e.getMarketDateTime()
                                                                        .toLocalDate()
                                                                        .equals(lastDate))
                                                        .collect(Collectors
                                                                        .toList());
                        Double open = lastDatePrices.get(0)
                                        .getRegularMarketPrice();
                        Double high = lastDatePrices.stream()
                                        .map(e -> e.getRegularMarketPrice())
                                        .max((a, b) -> (a.compareTo(b))).get();
                        Double low = lastDatePrices.stream()
                                        .map(e -> e.getRegularMarketPrice())
                                        .min(Comparator.naturalOrder()).get();
                        Double close = lastDatePrices
                                        .get(lastDatePrices.size() - 1)
                                        .getRegularMarketPrice();

                        return StockOHLCDTO.StockData.builder()
                                        .symbol(stockCode) //
                                        .timestamp(lastOHLCDate.atStartOfDay(
                                                        ZoneId.systemDefault())
                                                        .toInstant()
                                                        .toEpochMilli()) //
                                        .marketDate(lastDate) //
                                        .open(open) //
                                        .high(high) //
                                        .low(low) //
                                        .close(close) //
                                        .build();
                } else {
                        return null;
                }


        }

        private boolean getDatesInSameWeek(LocalDate date,
                        LocalDate referenceDate) {

                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int referenceWeek = referenceDate
                                .get(weekFields.weekOfWeekBasedYear());
                int referenceYear =
                                referenceDate.get(weekFields.weekBasedYear());

                int week = date.get(weekFields.weekOfWeekBasedYear());
                int year = date.get(weekFields.weekBasedYear());

                if (week == referenceWeek && year == referenceYear) {
                        return true;
                }

                return false;
        }
}
