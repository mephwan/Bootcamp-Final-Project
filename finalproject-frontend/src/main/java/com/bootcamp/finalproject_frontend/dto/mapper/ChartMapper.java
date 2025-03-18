package com.bootcamp.finalproject_frontend.dto.mapper;

import org.springframework.stereotype.Component;
import com.bootcamp.finalproject_frontend.dto.CandleStickDTO;
import com.bootcamp.finalproject_frontend.lib.DateManager;
import com.bootcamp.finalproject_frontend.lib.DateManager.Zone;
import com.bootcamp.finalproject_frontend.model.CandleStick;

@Component
public class ChartMapper {
  public CandleStickDTO map(CandleStick candle) {
    long unixtimeDate = DateManager.of(Zone.HK).convert(candle.getDate());
    return CandleStickDTO.builder()
    .date(unixtimeDate)
    .open(candle.getOpen())
    .high(candle.getHigh())
    .low(candle.getLow())
    .close(candle.getClose())
    .build();
  }
}
