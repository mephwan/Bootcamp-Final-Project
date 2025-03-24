package com.bootcamp.yahoofinance.service.impl;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;
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

                String redisString = "5min" + stockCode;

                StockDTO redisData = this.redisManager.get(redisString,
                                StockDTO.class);

                if (redisData != null) {
                        if (!redisData.getData().isEmpty()) {

                                Long redisLastUpdateTimeStamp = redisData
                                                .getData()
                                                .get(redisData.getData().size()
                                                                - 1)
                                                .getRegularMarketTime();

                                if (stockPriceRepository
                                                .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                                stockCode)
                                                .get().getRegularMarketTime()
                                                .equals(redisLastUpdateTimeStamp)) {

                                        System.out.println(LocalDateTime.now()
                                                        .getHour()
                                                        + ":"
                                                        + LocalDateTime.now()
                                                                        .getMinute()
                                                        + "  Get From Redis: "
                                                        + redisString);

                                        return redisData;
                                }
                        }
                }

                LocalDate lastTradeDate = this.stockPriceRepository
                                .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                stockCode)
                                .orElseThrow(() -> new BusinessException())
                                .getMarketDateTime().toLocalDate();

                StockDTO result = StockDTO.builder().symbol(stockCode)
                                .timeFrame("M5")
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

                System.out.println("Get From Database: " + redisString);

                this.redisManager.set(redisString, result,
                                Duration.ofMinutes(5));

                return result;
        }

        @Override
        public StockOHLCDTO getDayOHLC(String stockCode)
                        throws JsonProcessingException {

                LocalDate today = LocalDate.now();

                String redisString = "D1" + stockCode;

                StockOHLCDTO.StockData[] redisDataArr = this.redisManager.get(
                                redisString, StockOHLCDTO.StockData[].class);

                List<StockOHLCDTO.StockData> datas = new LinkedList<>();

                boolean isGetFromRedis = false;

                if (redisDataArr != null) {

                        datas = new ArrayList<>(Arrays.asList(redisDataArr));

                        LocalDate redisLastUpdate = datas.stream()
                                        .map(e -> e.getMarketDate())
                                        .max(Comparator.naturalOrder()).get();

                        if (this.stockPriceOHLCRepository
                                        .findFirstBySymbolOrderByTimestampDesc(
                                                        stockCode)
                                        .get().getDate()
                                        .equals(redisLastUpdate)) {

                                System.out.println(LocalDateTime.now().getHour()
                                                + ":"
                                                + LocalDateTime.now()
                                                                .getMinute()
                                                + "  Get From Redis: "
                                                + redisString);

                                isGetFromRedis = true;

                        }
                }

                if (isGetFromRedis) {
                        StockOHLCDTO.builder().symbol(stockCode).timeFrame("D1")
                                        .data(datas.stream().collect(Collectors
                                                        .toMap(StockOHLCDTO.StockData::getMarketDate,
                                                                        Function.identity(),
                                                                        (existing, replacement) -> existing))
                                                        .values().stream()
                                                        .collect(Collectors
                                                                        .toList()))
                                        .build();
                } else {

                        Long getFromTimeStamp = today.plusYears(-1)
                                        .withDayOfMonth(1)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().getEpochSecond();

                        List<StockPriceOHLCEntity> dailyPrices =
                                        this.stockPriceOHLCRepository
                                                        .findBySymbol(stockCode)
                                                        .stream()
                                                        .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                        .collect(Collectors
                                                                        .toList());

                        datas = dailyPrices.stream()
                                        .map(e -> StockOHLCDTO.StockData
                                                        .builder()
                                                        .symbol(stockCode)
                                                        .timestamp(e.getTimestamp()) //
                                                        .marketDate(e.getDate()) //
                                                        .open(e.getOpen()) //
                                                        .high(e.getHigh()) //
                                                        .low(e.getLow()) //
                                                        .close(e.getClose()) //
                                                        .build())
                                        .collect(Collectors.toList());

                        System.out.println(LocalDateTime.now().getHour() + ":"
                                        + LocalDateTime.now().getMinute()
                                        + "  Get From Database: "
                                        + redisString);

                        this.redisManager.set(redisString, datas.stream()
                                        .toArray(StockOHLCDTO.StockData[]::new),
                                        Duration.ofHours(12));
                }

                StockOHLCDTO.StockData lastUpdatePrice =
                                getLastUpdateOHLC(stockCode);

                if (lastUpdatePrice != null) {
                        datas.add(lastUpdatePrice);
                }

                List<StockOHLCDTO.StockData> dailyOHLC = new LinkedList<>();

                for (int i = 0; i < 360; i++) {

                        LocalDate date = today.minusDays(i);

                        Optional<StockOHLCDTO.StockData> oneDayOHLC = datas
                                        .stream() //
                                        .filter(e -> e.getMarketDate()
                                                        .equals(date)) //
                                        .max(Comparator.comparing(
                                                        StockOHLCDTO.StockData::getTimestamp));


                        if (oneDayOHLC.isPresent()) {

                                dailyOHLC.add(StockOHLCDTO.StockData.builder() //
                                                .symbol(stockCode) //
                                                .timestamp(oneDayOHLC.get()
                                                                .getTimestamp()) //
                                                .marketDate(oneDayOHLC.get()
                                                                .getMarketDate()) //
                                                .open(oneDayOHLC.get()
                                                                .getOpen()) //
                                                .high(oneDayOHLC.get()
                                                                .getHigh()) //
                                                .low(oneDayOHLC.get().getLow()) //
                                                .close(oneDayOHLC.get()
                                                                .getClose()) //
                                                .build());
                        }

                }

                return StockOHLCDTO.builder().symbol(stockCode).timeFrame("D1")
                                .data(dailyOHLC).build();
        }


        @Override
        public StockOHLCDTO getWeekOHLC(String stockCode)
                        throws JsonProcessingException {

                LocalDate today = LocalDate.now();

                String redisString = "D1" + stockCode;

                StockOHLCDTO.StockData[] redisDataArr = this.redisManager.get(
                                redisString, StockOHLCDTO.StockData[].class);

                List<StockOHLCDTO.StockData> datas = new LinkedList<>();

                boolean isGetFromRedis = false;

                if (redisDataArr != null) {

                        datas = new ArrayList<>(Arrays.asList(redisDataArr));

                        LocalDate redisLastUpdate = datas.stream()
                                        .map(e -> e.getMarketDate())
                                        .max(Comparator.naturalOrder()).get();

                        if (this.stockPriceOHLCRepository
                                        .findFirstBySymbolOrderByTimestampDesc(
                                                        stockCode)
                                        .get().getDate()
                                        .equals(redisLastUpdate)) {

                                System.out.println(LocalDateTime.now().getHour()
                                                + ":"
                                                + LocalDateTime.now()
                                                                .getMinute()
                                                + "  Get From Redis: "
                                                + redisString);

                                isGetFromRedis = true;
                        }

                }

                if (isGetFromRedis) {
                        StockOHLCDTO.builder().symbol(stockCode).timeFrame("D1")
                                        .data(datas.stream().collect(Collectors
                                                        .toMap(StockOHLCDTO.StockData::getMarketDate,
                                                                        Function.identity(),
                                                                        (existing, replacement) -> existing))
                                                        .values().stream()
                                                        .collect(Collectors
                                                                        .toList()))
                                        .build();
                } else {


                        Long getFromTimeStamp = today.plusYears(-1)
                                        .withDayOfMonth(1)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().getEpochSecond();

                        List<StockPriceOHLCEntity> dailyPrices =
                                        this.stockPriceOHLCRepository
                                                        .findBySymbol(stockCode)
                                                        .stream()
                                                        .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                        .collect(Collectors
                                                                        .toList());

                        datas = dailyPrices.stream()
                                        .map(e -> StockOHLCDTO.StockData
                                                        .builder()
                                                        .symbol(stockCode)
                                                        .timestamp(e.getTimestamp()) //
                                                        .marketDate(e.getDate()) //
                                                        .open(e.getOpen()) //
                                                        .high(e.getHigh()) //
                                                        .low(e.getLow()) //
                                                        .close(e.getClose()) //
                                                        .build())
                                        .collect(Collectors.toList());

                        System.out.println(LocalDateTime.now().getHour() + ":"
                                        + LocalDateTime.now().getMinute()
                                        + "  Get From Database: "
                                        + redisString);

                        this.redisManager.set(redisString, datas.stream()
                                        .toArray(StockOHLCDTO.StockData[]::new),
                                        Duration.ofHours(12));

                }

                StockOHLCDTO.StockData lastUpdatePrice =
                                getLastUpdateOHLC(stockCode);

                if (lastUpdatePrice != null) {
                        datas.add(lastUpdatePrice);
                }

                List<StockOHLCDTO.StockData> weeklyOHLC = new LinkedList<>();

                for (int i = 0; i < 52; i++) {

                        LocalDate weekDate = today.minusWeeks(i);

                        if (stockCode.contains("HK")
                                        || stockCode.equals("BTC-USD")
                                        || stockCode.equals("ETH-USD")) {
                                if (weekDate.getDayOfWeek()
                                                .equals(DayOfWeek.SUNDAY)) {
                                        weekDate = weekDate.plusWeeks(-1);
                                }
                        } else {
                                if (weekDate.getDayOfWeek()
                                                .equals(DayOfWeek.SUNDAY)
                                                || weekDate.getDayOfWeek()
                                                                .equals(DayOfWeek.MONDAY)) {
                                        weekDate = weekDate.plusWeeks(-1);
                                }
                        }

                        LocalDate finalWeekDate = weekDate;

                        List<StockOHLCDTO.StockData> oneWeekOHLC = datas
                                        .stream() //
                                        .filter(e -> getDatesInSameWeek(
                                                        e.getMarketDate(),
                                                        finalWeekDate)) //
                                        .collect(Collectors.toList());

                        Double open = oneWeekOHLC.get(0).getOpen();

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

                return StockOHLCDTO.builder().symbol(stockCode).timeFrame("W1")
                                .data(weeklyOHLC).build();
        }

        @Override
        public StockOHLCDTO getMonthOHLC(String stockCode)
                        throws JsonProcessingException {

                LocalDate today = LocalDate.now();

                String redisString = "MN" + stockCode;

                StockOHLCDTO.StockData[] redisDataArr = this.redisManager.get(
                                redisString, StockOHLCDTO.StockData[].class);

                List<StockOHLCDTO.StockData> datas = new LinkedList<>();

                boolean isGetFromRedis = false;

                if (redisDataArr != null) {

                        datas = new ArrayList<>(Arrays.asList(redisDataArr));

                        LocalDate redisLastUpdate = datas.stream()
                                        .map(e -> e.getMarketDate())
                                        .max(Comparator.naturalOrder()).get();

                        if (this.stockPriceOHLCRepository
                                        .findFirstBySymbolOrderByTimestampDesc(
                                                        stockCode)
                                        .get().getDate()
                                        .equals(redisLastUpdate)) {

                                System.out.println(LocalDateTime.now().getHour()
                                                + ":"
                                                + LocalDateTime.now()
                                                                .getMinute()
                                                + "  Get From Redis: "
                                                + redisString);

                                isGetFromRedis = true;
                        }

                }

                if (isGetFromRedis) {
                        StockOHLCDTO.builder().symbol(stockCode).timeFrame("D1")
                                        .data(datas.stream().collect(Collectors
                                                        .toMap(StockOHLCDTO.StockData::getMarketDate,
                                                                        Function.identity(),
                                                                        (existing, replacement) -> existing))
                                                        .values().stream()
                                                        .collect(Collectors
                                                                        .toList()))
                                        .build();
                } else {

                        Long getFromTimeStamp = today.plusYears(-2)
                                        .withDayOfMonth(1)
                                        .atStartOfDay(ZoneId.systemDefault())
                                        .toInstant().getEpochSecond();

                        List<StockPriceOHLCEntity> dailyPrices =
                                        this.stockPriceOHLCRepository
                                                        .findBySymbol(stockCode)
                                                        .stream()
                                                        .filter(e -> e.getTimestamp() >= getFromTimeStamp)
                                                        .collect(Collectors
                                                                        .toList());

                        datas = dailyPrices.stream()
                                        .map(e -> StockOHLCDTO.StockData
                                                        .builder()
                                                        .symbol(stockCode)
                                                        .timestamp(e.getTimestamp()) //
                                                        .marketDate(e.getDate()) //
                                                        .open(e.getOpen()) //
                                                        .high(e.getHigh()) //
                                                        .low(e.getLow()) //
                                                        .close(e.getClose()) //
                                                        .build())
                                        .collect(Collectors.toList());

                        System.out.println(LocalDateTime.now().getHour() + ":"
                                        + LocalDateTime.now().getMinute()
                                        + "  Get From Database: "
                                        + redisString);

                        this.redisManager.set(redisString, datas.stream()
                                        .toArray(StockOHLCDTO.StockData[]::new),
                                        Duration.ofHours(12));
                }

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
                ZoneId zone = ZoneId.systemDefault();

                if (!stockCode.contains("HK")) {
                        zone = ZoneId.of("America/New_York");
                }
                LocalDate lastDate = this.stockPriceRepository
                                .findFirstBySymbolOrderByMarketDateTimeDesc(
                                                stockCode)
                                .get().getMarketDateTime().toLocalDate();

                List<StockPriceEntity> lastDatePrices =
                                this.stockPriceRepository
                                                .findBySymbol(stockCode)
                                                .stream()
                                                .filter(e -> e.getMarketDateTime()
                                                                .toLocalDate()
                                                                .equals(lastDate))
                                                .sorted((a, b) -> {
                                                        Long aStamp = a.getRegularMarketTime();
                                                        Long bStamp = b.getRegularMarketTime();
                                                        return aStamp.compareTo(
                                                                        bStamp);
                                                }).collect(Collectors.toList());

                Double open = lastDatePrices.get(0).getRegularMarketPrice();
                Double high = lastDatePrices.stream()
                                .map(e -> e.getRegularMarketPrice())
                                .max((a, b) -> (a.compareTo(b))).get();
                Double low = lastDatePrices.stream()
                                .map(e -> e.getRegularMarketPrice())
                                .min(Comparator.naturalOrder()).get();
                Double close = lastDatePrices.get(lastDatePrices.size() - 1)
                                .getRegularMarketPrice();


                return StockOHLCDTO.StockData.builder().symbol(stockCode) //
                                .timestamp(lastDate.atStartOfDay(zone)
                                                .plusDays(1).minusMinutes(30)
                                                .toEpochSecond()) //
                                .marketDate(lastDate) //
                                .open(open) //
                                .high(high) //
                                .low(low) //
                                .close(close) //
                                .build();



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
