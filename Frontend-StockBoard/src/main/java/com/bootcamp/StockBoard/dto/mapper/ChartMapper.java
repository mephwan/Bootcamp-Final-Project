package com.bootcamp.StockBoard.dto.mapper;

import org.springframework.stereotype.Component;
import com.bootcamp.StockBoard.dto.CandleStickDTO;
import com.bootcamp.StockBoard.dto.LinePointDTO;
import com.bootcamp.StockBoard.lib.DateManager;
import com.bootcamp.StockBoard.lib.DateManager.Zone;
import com.bootcamp.StockBoard.model.CandleStick;
import com.bootcamp.StockBoard.model.LinePoint;

@Component
public class ChartMapper {
  public CandleStickDTO map(CandleStick candle) {
    long unixtimeDate = DateManager.of(Zone.HK).convert(candle.getDate());
    return CandleStickDTO.builder() //
        .date(unixtimeDate) //
        .open(candle.getOpen()) //
        .close(candle.getClose()) //
        .high(candle.getHigh()) //
        .low(candle.getLow()) //
        .build();
  }

  public LinePointDTO map(LinePoint point) {
    long unixtimeDatetime =
        DateManager.of(Zone.HK).convert(point.getDateTime());
    return LinePointDTO.builder() //
        .dateTime(unixtimeDatetime) //
        .close(point.getClose()) //
        .build();
  }
}
