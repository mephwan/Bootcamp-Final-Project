package com.bootcamp.yahoofinance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.DTO.StockDTO.StockData;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.bootcamp.yahoofinance.service.YahooService;

@Service
public class YahooServiceImpl implements YahooService {
    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Override
    public StockDTO getPrice(String stockCode) {

        LocalDate lastTradeDate = this.stockPriceRepository
                .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode)
                .orElseThrow(() -> new BusinessException()).getMarketDateTime()
                .toLocalDate();

        return StockDTO.builder()
        .symbol(stockCode)
        .timeFrame("M5")
        .data(this.stockPriceRepository.findBySymbol(stockCode).stream()
        .filter(e -> e.getMarketDateTime().toLocalDate().equals(lastTradeDate))
        .map(e -> StockData.builder().regularMarketTime(e.getRegularMarketTime())
        .marketDateTime(e.getMarketDateTime()).regularMarketPrice(e.getRegularMarketPrice())
        .regularMarketChangePercent(e.getRegularMarketChangePercent()).build()).collect(Collectors.toList())).build();
        
    }
}
