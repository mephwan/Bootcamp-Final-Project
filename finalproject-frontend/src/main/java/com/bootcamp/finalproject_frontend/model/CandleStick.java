package com.bootcamp.finalproject_frontend.model;

import java.time.LocalDate;
import lombok.Getter;

@Getter
public class CandleStick {
  private LocalDate date;
  private Double open;
  private Double high;
  private Double low;
  private Double close;

  public CandleStick(int year, int month, int dayOfMonth, Double open, Double high, Double low, Double close) {
    this.date = LocalDate.of(year, month, dayOfMonth);
    this.open = open;
    this.high = high;
    this.low = low;
    this.close = close;
  }

  public enum TYPE {
    DAY,WEEK,MONTH,;

    public static CandleStick.TYPE of(String type) {
      return switch (type) {
        case "1d" -> CandleStick.TYPE.DAY;
        case "1w" -> CandleStick.TYPE.WEEK;
        case "mn" -> CandleStick.TYPE.MONTH;
        default -> throw new RuntimeException();
      };
    }
  }
}
