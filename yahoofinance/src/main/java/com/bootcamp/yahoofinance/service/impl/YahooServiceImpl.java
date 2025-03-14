package com.bootcamp.yahoofinance.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bootcamp.yahoofinance.DTO.StockDTO;
import com.bootcamp.yahoofinance.exception.BusinessException;
import com.bootcamp.yahoofinance.repository.StockPriceRepository;
import com.bootcamp.yahoofinance.service.YahooService;

@Service
public class YahooServiceImpl implements YahooService {
    @Autowired
    private StockPriceRepository stockPriceRepository;

    @Override
    public List<StockDTO> getPrice(String stockCode) {

        LocalDate lastTradeDate = this.stockPriceRepository
                .findFirstBySymbolOrderByMarketDateTimeDesc(stockCode)
                .orElseThrow(() -> new BusinessException()).getMarketDateTime()
                .toLocalDate();

        return this.stockPriceRepository.findBySymbol(stockCode).stream()
                .filter(e -> e.getMarketDateTime().toLocalDate()
                        .equals(lastTradeDate))
                .map(e -> StockDTO.builder().symbol(e.getSymbol())
                        .marketDateTime(e.getMarketDateTime())
                        .regularMarketPrice(e.getRegularMarketPrice())
                        .type(e.getType()).build())
                .collect(Collectors.toList());
    }
}
