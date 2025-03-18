package com.bootcamp.finalproject_frontend.dto.mapper;

import org.springframework.stereotype.Component;
import com.bootcamp.finalproject_frontend.dto.CandleStickDTO;
import com.bootcamp.finalproject_frontend.dto.LinePointDTO;
import com.bootcamp.finalproject_frontend.lib.DateManager;
import com.bootcamp.finalproject_frontend.lib.DateManager.Zone;
import com.bootcamp.finalproject_frontend.model.CandleStick;
import com.bootcamp.finalproject_frontend.model.LinePoint;

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
