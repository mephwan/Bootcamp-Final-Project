package com.bootcamp.yahoofinance.service.impl;

import java.time.LocalDate;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.lib.RedisManager;
import com.bootcamp.yahoofinance.lib.YahooManager;
import com.bootcamp.yahoofinance.model.YahooOHLCDto;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.bootcamp.yahoofinance.service.YahooService;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class YahooServiceImpl implements YahooService {
    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private YahooManager YahooManager;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public StockDTO getPrice(String stockCode) throws JsonProcessingException {

        String redisString = "5min" + stockCode;

        StockDTO redisData = this.redisManager.get(redisString,StockDTO.class);

        if (redisData != null) {
            StockDTO stockDTO = StockDTO.builder().symbol(redisData.getSymbol()).timeFrame("M5")
            .data(redisData.getData().stream().filter(e -> e.getSymbol().equals(stockCode)).collect(Collectors.toList()))
            .build();

            System.out.println("Get From Redis.....");

            return stockDTO;
        }

        LocalDate lastTradeDate = this.stockPriceRepository
                .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode)
                .orElseThrow(() -> new BusinessException()).getMarketDateTime()
                .toLocalDate();

        return StockDTO.builder()
        .symbol(stockCode)
        .timeFrame("M5")
        .data(this.stockPriceRepository.findBySymbol(stockCode).stream()
        .filter(e -> e.getMarketDateTime().toLocalDate().equals(lastTradeDate))
        .map(e -> StockData.builder().symbol(stockCode).regularMarketTime(e.getRegularMarketTime())
        .marketDateTime(e.getMarketDateTime()).regularMarketPrice(e.getRegularMarketPrice())
        .regularMarketChangePercent(e.getRegularMarketChangePercent()).build()).collect(Collectors.toList())).build();
        
    }

    @Override
    public YahooOHLCDto getOhlcDto() {

        return this.YahooManager.getOhlcDto(this.restTemplate);
    }
}
